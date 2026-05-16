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

package com.frybits.starrynight.atproto

import com.frybits.starrynight.atproto.models.ATProtoSession
import com.frybits.starrynight.atproto.models.ResolvedDid

public interface ATProtoRepository {

    public suspend fun resolveHandle(username: String): Result<String>

    public suspend fun resolveDid(did: String): Result<ResolvedDid>

    public suspend fun createSession(username: String, password: String): Result<ATProtoSession>

    public suspend fun getSession(session: ATProtoSession): Result<ATProtoSession>

    public suspend fun refreshSession(session: ATProtoSession): Result<ATProtoSession>

    public suspend fun deleteSession(session: ATProtoSession): Result<Unit>
}
