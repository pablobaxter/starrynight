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

package com.frybits.starrynight.android.network.core.impl.dns

internal data class DnsHeader(
    val id: Int, // identifier assigned by program that generated query, copied to reply
    val qr: Boolean, // one-bit field that specifies if message is query (0, false) or response (1, true)
    val opCode: Int,  // kind of query
    val aa: Boolean,  // only in response, specifies that responding server is authority for domain name (corresponds to name matching query or first owner name in answer)
    val tc: Boolean,  // TrunCation, specifies message was truncated due to length greater than permitted on the transmission channel
    val rd: Boolean,  // Recursion Desired: set in query, copied to response, directs name server to pursue query recursively
    val ra: Boolean,  // Recursion Available, set (1) or cleared (0) in response, denotes whether name server supports recursive queries
    val rCode: Int,  // reserved, must be 0
    val qdCount: Int,  // response code
    val anCount: Int,  // number of entries in questions section
    val nsCount: Int,  // number of RRs in answer section
    val arCount: Int  // number of RRs in authority records section
)