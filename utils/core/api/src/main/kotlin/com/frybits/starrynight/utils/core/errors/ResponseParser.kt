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

package com.frybits.starrynight.utils.core.errors

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Response

private val json = Json
public fun Response<*>.parseException(error: String?): StarryNightNetworkException {
    return when (val code = code()) {
        400 -> {
            val errorJson = error?.let { runCatching { json.decodeFromString<JsonObject>(it) }.getOrNull() }
            BadRequestNetworkException(code, errorJson?.let { "${errorJson["error"]?.jsonPrimitive?.content}: ${errorJson["message"]?.jsonPrimitive?.content}" } ?: error ?: "No error body")
        }
        401 -> {
            val errorJson = error?.let { runCatching { json.decodeFromString<JsonObject>(it) }.getOrNull() }
            UnauthorizedNetworkException(code, errorJson?.let { "${errorJson["error"]?.jsonPrimitive?.content}: ${errorJson["message"]?.jsonPrimitive?.content}" } ?: error ?: "No error body")
        }
        404 -> {
            NotFoundNetworkException(code, error ?: "No error body")
        }
        else -> {
            UnknownNetworkException(code, error ?: "No error body")
        }
    }
}
