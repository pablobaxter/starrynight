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

package com.frybits.starrynight.android.atproto.network

import com.frybits.starrynight.atproto.network.models.PlcData
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

internal interface ATProtoNetworkApi {

    @GET("https://{host}/.well-known/atproto-did")
    suspend fun resolveHandleViaWellKnown(@Path("host") host: String): Response<String>

    @GET("https://plc.directory/{did}/data")
    suspend fun resolveDidViaPlcDirectory(@Path("did") did: String): Response<PlcData>

    @GET("https://{host}/{user}/did.json")
    suspend fun resolveDidViaHost(@Path("host") host: String, @Path("user", encoded = true) user: String): Response<JsonObject>
}
