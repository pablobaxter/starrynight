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

internal sealed interface RecordData

@JvmInline
internal value class CNAME(val cName: String): RecordData

internal data class MX(
    val preference: Int,
    val exchange: String
): RecordData

@JvmInline
internal value class NS(val nsName: String): RecordData

@JvmInline
internal value class TXT(val txt: List<String>): RecordData

@JvmInline
internal value class A(val a: String?): RecordData

@JvmInline
internal value class AAAA(val aaaa: String?): RecordData
