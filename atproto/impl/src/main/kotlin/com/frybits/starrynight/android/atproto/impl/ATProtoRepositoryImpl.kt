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

package com.frybits.starrynight.android.atproto.impl

import com.atproto.server.createSession.CreateSessionApi
import com.atproto.server.createSession.CreateSessionRequest
import com.frybits.starrynight.android.atproto.models.strings.Did
import com.frybits.starrynight.android.atproto.network.ATProtoNetworkApi
import com.frybits.starrynight.atproto.ATProtoRepository
import com.frybits.starrynight.atproto.models.ATProtoSession
import com.frybits.starrynight.android.atproto.models.PlcData
import com.frybits.starrynight.android.atproto.models.Service
import com.frybits.starrynight.atproto.data.UserDao
import com.frybits.starrynight.atproto.data.models.UserRoomData
import com.frybits.starrynight.atproto.models.ATProtoUserData
import com.frybits.starrynight.network.DnsRecordRepository
import com.frybits.starrynight.network.models.TXT
import com.frybits.starrynight.utils.core.IODispatcher
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.util.logging.Logger
import kotlin.time.Clock

private val LOGGER = Logger.getLogger("ATProtoRepository")

@ContributesBinding(AppScope::class)
@Inject
internal class ATProtoRepositoryImpl(
    private val atProtoServicesApi: ATProtoNetworkApi,
    private val createSessionApi: CreateSessionApi,
    private val dnsRecordRepository: DnsRecordRepository,
    private val userDao: UserDao,
    private val json: Json,
    @param:IODispatcher private val ioDispatcher: CoroutineDispatcher
): ATProtoRepository {

    override suspend fun resolveHandle(username: String): Result<String> {
        return resolveHandleViaDNS(username).recover {
            currentCoroutineContext().ensureActive()
            LOGGER.warning("Failed resolution via dns: ${it.message}")

            val response = atProtoServicesApi.resolveHandleViaWellKnown(username)
            if (response.isSuccessful) {
                val did = response.body() ?: throw Exception("Empty did returned")
                return@recover Did(did)
            } else {
                val error = response.errorBody()?.use { errorBody ->
                    withContext(ioDispatcher) { errorBody.string() }
                } ?: throw Exception("Unknown error (${response.code()}) - No error body")

                throw Exception("Unknown error (${response.code()}) - $error")
            }

        }.map { it.toString() }
    }

    override suspend fun resolveDid(did: String): Result<ATProtoUserData> {
        return runCatching {
            val didSplit = did.split(':')
            require(didSplit.size >= 3) { "Did must contain at least 3 parts" }

            when (val type = didSplit[1]) {
                "plc" -> {
                    val response = atProtoServicesApi.resolveDidViaPlcDirectory(did)
                    if (response.isSuccessful) {
                        val plcData = response.body() ?: throw Exception("Empty pld data returned")
                        require(plcData.did == did) { "Did does not match plc did: plc=${plcData.did}, did=$did" }
                        val userData = UserRoomData(
                            handle = plcData.alsoKnownAs.toSet(),
                            did = plcData.did,
                            pds = plcData.services.mapNotNull {
                                if (it.key == "atproto_pds" && it.value.type == "AtprotoPersonalDataServer") {
                                    return@mapNotNull it.value.endpoint
                                }
                                return@mapNotNull null
                            }.firstOrNull().orEmpty(),
                            lastUpdated = Clock.System.now()
                        )
                        userDao.insertUser(userData)
                        ATProtoUserData(
                            handle = userData.handle,
                            did = userData.did,
                            pds = userData.pds,
                            lastUpdated = userData.lastUpdated
                        )
                    } else {
                        val error = response.errorBody()?.use { errorBody ->
                            withContext(ioDispatcher) { errorBody.string() }
                        } ?: throw Exception("Unknown error (${response.code()}) - No error body")

                        throw Exception("Unknown error (${response.code()}) - $error")
                    }
                }
                "web" -> {
                    val host = didSplit[2].replace("%3A", ":")
                    val userPath = didSplit.drop(3).joinToString("/").ifEmpty { ".well-known" }
                    val response = atProtoServicesApi.resolveDidViaHost(host, userPath)
                    if (response.isSuccessful) {
                        val plcDataJson = response.body() ?: throw Exception("Empty pld data returned")
                        val plcDid = plcDataJson["id"]?.jsonPrimitive?.content.orEmpty()
                        val alsoKnownAs = plcDataJson["alsoKnownAs"]?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty()
                        val services = buildMap {
                            plcDataJson["service"]?.jsonArray?.forEach { jsonElement ->
                                val id = jsonElement.jsonObject["id"]?.jsonPrimitive?.content?.removePrefix("#") ?: return@forEach
                                val type = jsonElement.jsonObject["type"]?.jsonPrimitive?.content ?: return@forEach
                                val serviceEndpoint = jsonElement.jsonObject["serviceEndpoint"]?.jsonPrimitive?.content ?: return@forEach
                                put(id, Service(type, serviceEndpoint))
                            }
                        }
                        require(plcDid == did) { "Did does not match plc did: plc=${plcDid}, did=$did" }
                        val plcData = PlcData(
                            did = plcDid,
                            rotationKeys = emptyList(),
                            verificationMethods = emptyMap(),
                            alsoKnownAs = alsoKnownAs,
                            services = services
                        )
                        val userData = UserRoomData(
                            handle = plcData.alsoKnownAs.firstOrNull().orEmpty(),
                            did = plcData.did,
                            pds = plcData.services.mapNotNull {
                                if (it.key == "atproto_pds" && it.value.type == "AtprotoPersonalDataServer") {
                                    return@mapNotNull it.value.endpoint
                                }
                                return@mapNotNull null
                            }.firstOrNull().orEmpty(),
                            lastUpdated = Clock.System.now()
                        )
                        userDao.insertUser(userData)
                        ATProtoUserData(
                            handle = userData.handle,
                            did = userData.did,
                            pds = userData.pds,
                            lastUpdated = userData.lastUpdated
                        )
                    } else {
                        val error = response.errorBody()?.use { errorBody ->
                            withContext(ioDispatcher) { errorBody.string() }
                        } ?: throw Exception("Unknown error (${response.code()}) - No error body")

                        throw Exception("Unknown error (${response.code()}) - $error")
                    }
                }
                else -> throw Exception("Unknown type for did=$did - type: $type")
            }
        }
    }

    override suspend fun createSession(pds: String, username: String, password: String): Result<ATProtoSession> {
        return runCatching {
            val host = URI.create(pds).host
            val response = createSessionApi.createSession(host, CreateSessionRequest(password, username))
            if (response.isSuccessful) {
                val sessionResponse =
                    response.body() ?: throw Exception("No sessions response found")
                val alsoKnownAsList = emptyList<String>()
                return@runCatching ATProtoSession(
                    id = sessionResponse.did.toString(),
                    email = sessionResponse.email,
                    active = sessionResponse.active,
                    alsoKnownAs = alsoKnownAsList,
                    handle = sessionResponse.handle.toString(),
                    status = null,
                    accessJwt = sessionResponse.accessJwt,
                    refreshJwt = sessionResponse.refreshJwt,
                    emailConfirmed = sessionResponse.emailConfirmed,
                    emailAuthFactor = sessionResponse.emailAuthFactor
                )
            } else {
                val error = response.errorBody()?.use { errorBody ->
                    withContext(ioDispatcher) { errorBody.string() }
                } ?: throw Exception("Unknown error (${response.code()}) - No error body")

                if (response.code() == 400) {
                    val errorJson = json.decodeFromString<JsonObject>(error)
                    throw Exception("Bad request (400) - ${errorJson["error"]?.jsonPrimitive?.content}: ${errorJson["message"]?.jsonPrimitive?.content}")
                } else if (response.code() == 401) {
                    val errorJson = json.decodeFromString<JsonObject>(error)
                    throw Exception("Unauthorized (401) - ${errorJson["error"]?.jsonPrimitive?.content}: ${errorJson["message"]?.jsonPrimitive?.content}")
                }

                throw Exception("Unknown error (${response.code()}) - $error")
            }
        }
    }

    private suspend fun resolveHandleViaDNS(username: String): Result<Did> {
        return dnsRecordRepository.fetchDnsMessage(username).mapCatching { message ->
            val didTxt = message.answer.map { it.data }
                .filterIsInstance<TXT>()
                .flatMap { it.txt }
                .firstOrNull { it.trim('"').startsWith("did=") } ?: throw Exception("No did object found")

            return@mapCatching Did(didTxt.removePrefix("did="))
        }
    }
}
