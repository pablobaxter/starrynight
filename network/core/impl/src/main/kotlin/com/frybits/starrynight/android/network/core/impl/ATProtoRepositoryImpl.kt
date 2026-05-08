/*
 * Starry Nights - A BlueSky Android Client
 * Copyright (C) 2026 pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.frybits.starrynight.android.network.core.impl

import android.annotation.SuppressLint
import android.net.DnsResolver
import android.os.CancellationSignal
import android.util.Log
import com.frybits.starrynight.atproto.models.strings.Did
import com.frybits.starrynight.network.core.ATProtoRepository
import com.frybits.starrynight.android.network.core.impl.dns.TXT
import com.frybits.starrynight.utils.core.IODispatcher
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val LOG_TAG = "ATProtoRepository"

@ContributesBinding(AppScope::class)
@Inject
internal class ATProtoRepositoryImpl(
    private val okHttpClient: OkHttpClient,
    private val dnsResolver: DnsResolver,
    @param:IODispatcher private val ioDispatcher: CoroutineDispatcher,
): ATProtoRepository {

    private val limitedDispatcherExecutor = ioDispatcher.limitedParallelism(1).asExecutor()

    override suspend fun resolveHandle(handle: String): Result<Did> {
        return runCatching { resolveHandleViaDNS(handle) }.recover {
            currentCoroutineContext().ensureActive()
            Log.w(LOG_TAG, "Failed resolution via dns", it)

            return@recover resolveHandleViaWellKnown(handle)
        }
    }

    private suspend fun resolveHandleViaWellKnown(handle: String): Did {
        val request = Request.Builder()
            .url(URL("https://$handle/.well-known/atproto-did"))
            .get()
            .build()

        return okHttpClient.newCall(request).executeAsync().use { response ->
            withContext(ioDispatcher) {
                response.body.use { Did(it.string()) }
            }
        }
    }

    private suspend fun resolveHandleViaDNS(handle: String): Did {
        return suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()

            @SuppressLint("WrongConstant")
            dnsResolver.rawQuery(
                null,
                "_atproto.$handle",
                DnsResolver.CLASS_IN,
                16,
                DnsResolver.FLAG_EMPTY,
                limitedDispatcherExecutor,
                cancellationSignal,
                object : DnsResolver.Callback<ByteArray> {
                    override fun onAnswer(answer: ByteArray, rcode: Int) {
                        val message = parseMessage(answer)
                        val didTxt = message.answer.map { it.data }.filterIsInstance<TXT>().flatMap { it.txt }.firstOrNull { it.startsWith("did=") }
                        if (didTxt == null) {
                            continuation.resumeWithException(Exception("No did object found"))
                        } else {
                            continuation.resume(Did(didTxt.removePrefix("did=")))
                        }
                    }

                    override fun onError(error: DnsResolver.DnsException) {
                        continuation.resumeWithException(Exception("Unable to get did", error))
                    }
                }
            )

            continuation.invokeOnCancellation { cancellationSignal.cancel() }

            cancellationSignal.setOnCancelListener {
                continuation.cancel()
            }
        }
    }
}
