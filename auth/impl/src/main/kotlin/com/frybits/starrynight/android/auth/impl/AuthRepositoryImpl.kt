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

package com.frybits.starrynight.android.auth.impl

import androidx.core.net.toUri
import com.frybits.starrynight.android.auth.LoggedInUserDataStore
import com.frybits.starrynight.atproto.ATProtoRepository
import com.frybits.starrynight.auth.AuthRepository
import com.frybits.starrynight.auth.LoggedInUserData
import com.frybits.starrynight.auth.data.UserDao
import com.frybits.starrynight.auth.data.models.UserRoomData
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Clock

@ContributesBinding(AppScope::class)
@Inject
internal class AuthRepositoryImpl(
    private val atProtoRepository: ATProtoRepository,
    private val userDao: UserDao,
    private val loggedInUserDataStore: LoggedInUserDataStore
) : AuthRepository {

    override suspend fun login(handle: String, password: String): Result<Unit> {
        return runCatching {
            val did = atProtoRepository.resolveHandle(handle).getOrElse {
                throw Exception("Error resolving handle", it)
            }

            val plcData = atProtoRepository.resolveDid(did).getOrElse {
                throw Exception("Error resolving did", it)
            }

            require(handle in plcData.alsoKnownAs.map { it.toUri().host }.toSet()) { "Handle not listed in did doc" }
            val pdsService = requireNotNull(plcData.services["atproto_pds"]) { "No PDS service found for $handle, did=${plcData.did}. Services found: ${plcData.services}" }
            require(pdsService.type == "AtprotoPersonalDataServer") { "PDS service found does not match standard type: AtprotoPersonalDataServer. Type found: ${pdsService.type}" }

            userDao.insertUser(UserRoomData(
                handle = handle,
                did = plcData.did,
                pds = pdsService.endpoint,
                lastUpdated = Clock.System.now()
            ))

            val sessionData = atProtoRepository.createSession(pdsService.endpoint, handle, password).getOrElse {
                throw Exception("Error creating session", it)
            }

            loggedInUserDataStore.storeLoggedInUserData(
                LoggedInUserData(
                    did = sessionData.id,
                    handle = sessionData.handle,
                    email = sessionData.email,
                    active = sessionData.active,
                    status = sessionData.status?.name.orEmpty(),
                    token = sessionData.accessJwt,
                    refreshToken = sessionData.refreshJwt,
                    emailConfirmed = sessionData.emailConfirmed
                )
            )
        }
    }
}
