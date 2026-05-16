/*
 * Starry Nights - A BlueSky Android Client
 * Copyright (C) 2026 Pablo Baxter
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

package com.frybits.starrynight.android.atproto.utils

import com.atproto.server.createSession.CreateSessionResponse
import com.atproto.server.getSession.GetSessionResponse
import com.atproto.server.refreshSession.RefreshSessionResponse
import com.frybits.starrynight.atproto.models.ATProtoSession
import com.frybits.starrynight.atproto.models.ATProtoSessionStatus
import java.util.logging.Logger

private val LOGGER = Logger.getLogger("ATProtoSessionUtil")

internal fun CreateSessionResponse.toATProtoSession(): ATProtoSession {
    return ATProtoSession(
        id = did.toString(),
        email = email,
        active = active,
        handle = handle.toString(),
        status = runCatching { ATProtoSessionStatus.valueOf(status) }
            .onFailure { LOGGER.warning("Unable to find status for $status") }
            .getOrNull(),
        accessJwt = accessJwt,
        refreshJwt = refreshJwt,
        emailConfirmed = emailConfirmed,
        emailAuthFactor = emailAuthFactor
    )
}

internal fun GetSessionResponse.toATProtoSession(prevSession: ATProtoSession): ATProtoSession {
    return ATProtoSession(
        id = did.toString(),
        email = email,
        active = active,
        handle = handle.toString(),
        status = runCatching { ATProtoSessionStatus.valueOf(status) }
            .onFailure { LOGGER.warning("Unable to find status for $status") }
            .getOrNull(),
        accessJwt = prevSession.accessJwt,
        refreshJwt = prevSession.refreshJwt,
        emailConfirmed = emailConfirmed,
        emailAuthFactor = emailAuthFactor
    )
}

internal fun RefreshSessionResponse.toATProtoSession(): ATProtoSession {
    return ATProtoSession(
        id = did.toString(),
        email = email,
        active = active,
        handle = handle.toString(),
        status = runCatching { ATProtoSessionStatus.valueOf(status) }
            .onFailure { LOGGER.warning("Unable to find status for $status") }
            .getOrNull(),
        accessJwt = accessJwt,
        refreshJwt = refreshJwt,
        emailConfirmed = emailConfirmed,
        emailAuthFactor = emailAuthFactor
    )
}
