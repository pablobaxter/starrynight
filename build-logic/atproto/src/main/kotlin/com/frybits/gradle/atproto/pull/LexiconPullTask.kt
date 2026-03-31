package com.frybits.gradle.atproto.pull

import com.frybits.gradle.atproto.lexicon.RecordResponse
import com.frybits.gradle.atproto.lexicon.categories.ArrayField
import com.frybits.gradle.atproto.lexicon.categories.BodyField
import com.frybits.gradle.atproto.lexicon.categories.LexiconType
import com.frybits.gradle.atproto.lexicon.categories.MessageField
import com.frybits.gradle.atproto.lexicon.categories.ObjectField
import com.frybits.gradle.atproto.lexicon.categories.ParamsField
import com.frybits.gradle.atproto.lexicon.categories.PermissionSetField
import com.frybits.gradle.atproto.lexicon.categories.ProcedureField
import com.frybits.gradle.atproto.lexicon.categories.QueryField
import com.frybits.gradle.atproto.lexicon.categories.RecordField
import com.frybits.gradle.atproto.lexicon.categories.RefField
import com.frybits.gradle.atproto.lexicon.categories.SubscriptionField
import com.frybits.gradle.atproto.lexicon.categories.UnionField
import com.frybits.gradle.atproto.lexicon.lexiconJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Deque
import java.util.Hashtable
import java.util.LinkedList
import javax.inject.Inject
import javax.naming.directory.InitialDirContext
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@CacheableTask
public abstract class LexiconPullTask @Inject constructor(
    private val workExecutor: WorkerExecutor
) : DefaultTask() {

    init {
        group = "build"
        description = "Pull ATProto Lexicons"
    }

    @get:Input
    public abstract val endpoint: Property<String>

    @get:Input
    public abstract val nsids: SetProperty<String>

    @get:OutputDirectory
    public abstract val outputDir: DirectoryProperty

    @TaskAction
    internal fun pull() {
        if (nsids.get().isEmpty()) throw StopExecutionException("No NSIDs found, skipping lexicon pull task.")
        if (endpoint.get().isBlank()) throw StopExecutionException("No endpoint, skipping lexicon pull task.")
        val queue = workExecutor.noIsolation()

        // Queue for current NSIDs to check
        val nsidQueue = LinkedList(nsids.get())
        val seen = hashSetOf<String>()

        // Outer loop for when everything has been checked
        while (nsidQueue.isNotEmpty()) {
            // Refresh the cache each loop
            val cachedFilesSet = outputDir.asFileTree.map { it.name }.toSet()

            // Inner loop for current frame of NSIDs to check
            while (nsidQueue.isNotEmpty()) {
                val nsid = nsidQueue.pop()
                if (!seen.add(nsid)) continue // Avoid seen NSIDs
                if (nsid in cachedFilesSet) continue // Avoid NSIDs already downloaded

                // Queue the download for the lexicon associated to this NSID
                queue.submit(PullLexiconWorker::class) {
                    host.set(endpoint.get())
                    destinationFile.set(outputDir.file(nsid))
                    lexiconPath.set(nsid)
                }
            }

            // Wait for files to populate
            queue.await()

            // Grab all the record responses in the output directory
            val serializedLexicons = outputDir.asFileTree.map { file ->
                file.inputStream().use { stream ->
                    @OptIn(ExperimentalSerializationApi::class)
                    lexiconJson.decodeFromStream<RecordResponse>(stream)
                }
            }

            // Recursively find references that need to be checked and add them to the queue.
            // Use the cached files to ensure we don't add references that have already been downloaded.
            serializedLexicons.forEach {
                it.value.defs.values.forEach { def ->
                    collectRefs(def, nsidQueue, cachedFilesSet)
                }
            }
        }
    }

    // Recursively collect all references for the given lexicon type
    private fun collectRefs(lexiconType: LexiconType, collector: Deque<String>, cachedFilesSet: Set<String>) {
        when (lexiconType) {
            is RefField -> {
                val nsid = lexiconType.ref.substringBefore('#')
                if (nsid.isNotEmpty() && nsid !in cachedFilesSet) {
                    collector.push(nsid)
                }
            }

            is UnionField -> {
                val nsids = lexiconType.refs.map { it.substringBefore('#') }.filter { it.isNotEmpty() }
                nsids.forEach { nsid ->
                    if (nsid !in cachedFilesSet) {
                        collector.push(nsid)
                    }
                }
            }

            is ArrayField -> {
                collectRefs(lexiconType.items, collector, cachedFilesSet)
            }

            is ObjectField -> {
                lexiconType.properties.values.forEach {
                    collectRefs(it, collector, cachedFilesSet)
                }
            }

            is PermissionSetField -> {
                lexiconType.permissions.forEach {
                    collectRefs(it, collector, cachedFilesSet)
                }
            }

            is BodyField -> {
                lexiconType.schema?.let { collectRefs(it, collector, cachedFilesSet) }
            }

            is SubscriptionField -> {
                lexiconType.message.schema?.let { collectRefs(it, collector, cachedFilesSet) }
                lexiconType.parameters?.let { collectRefs(it, collector, cachedFilesSet) }
            }

            is MessageField -> {
                lexiconType.schema?.let { collectRefs(it, collector, cachedFilesSet) }
            }

            is RecordField -> {
                collectRefs(lexiconType.record, collector, cachedFilesSet)
            }

            is ProcedureField -> {
                lexiconType.parameters?.let { collectRefs(it, collector, cachedFilesSet) }
                lexiconType.output?.schema?.let { collectRefs(it, collector, cachedFilesSet) }
                lexiconType.input?.schema?.let { collectRefs(it, collector, cachedFilesSet) }
            }

            is QueryField -> {
                lexiconType.parameters?.let { collectRefs(it, collector, cachedFilesSet) }
                lexiconType.output?.schema?.let { collectRefs(it, collector, cachedFilesSet) }
            }

            is ParamsField -> {
                lexiconType.properties.values.forEach { collectRefs(it, collector, cachedFilesSet) }
            }

            else -> return
        }
    }
}

private interface PullLexiconParameters : WorkParameters {
    val host: Property<String>
    val lexiconPath: Property<String>
    val destinationFile: RegularFileProperty
}

private abstract class PullLexiconWorker : WorkAction<PullLexiconParameters> {

    private val logger = LoggerFactory.getLogger(LexiconPullTask::class.java) as Logger

    override fun execute() {
        val nsid = parameters.lexiconPath.get()
        logger.lifecycle("Pulling lexicon for $nsid")

        // Path to get DNS TXT record
        val authority = "_lexicon.${nsid.split('.').dropLast(1).reversed().joinToString(".")}"

        logger.info("Getting DNS TXT record for $nsid")

        val env = mapOf("java.naming.factory.initial" to "com.sun.jndi.dns.DnsContextFactory")
        val ictx = InitialDirContext(Hashtable(env))
        val attrs = ictx.getAttributes(authority, arrayOf("TXT"))

        // Search for the correct record
        val did = attrs.get("TXT").all.toList().mapNotNull { txt ->
            val didRecord = txt.toString()
            if (didRecord.startsWith("did=")) {
                return@mapNotNull didRecord.substringAfter('=')
            }
            return@mapNotNull null
        }.firstOrNull()

        // Exit early for missing did
        if (did == null) {
            logger.error("Did not find did $nsid")
            return
        }

        logger.info("DNS TXT record for $nsid: $did")

        val host = parameters.host.get()

        val uri = URI(
            "https",
            host,
            "/xrpc/com.atproto.repo.getRecord",
            "repo=$did&collection=com.atproto.lexicon.schema&rkey=$nsid",
            null
        )

        logger.info("Requesting lexicon from $uri")

        val request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Content-Type", "application/json")
            .GET()
            .build()

        HttpClient.newBuilder()
            .connectTimeout(30.seconds.toJavaDuration())
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build().use { client ->
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                when (val code = response.statusCode()) {
                    HttpURLConnection.HTTP_OK -> {
                        parameters.destinationFile.get().asFile.writeText(response.body())
                        logger.lifecycle("Lexicon $nsid successfully pulled")
                    }

                    HttpURLConnection.HTTP_NOT_FOUND -> {
                        logger.error("Lexicon $nsid not found")
                    }

                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        logger.error("Lexicon $nsid unauthorized")
                    }

                    else -> {
                        logger.error("Error while pulling lexicon for $nsid. HTTP code: $code, response: ${response.body()}")
                    }
                }
            }
    }
}
