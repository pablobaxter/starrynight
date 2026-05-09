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

package com.frybits.starrynight.network.models

public sealed interface RecordData

@JvmInline
public value class CNAME(public val cName: String): RecordData

public data class MX(
    val preference: Int,
    val exchange: String
): RecordData

@JvmInline
public value class NS(public val nsName: String): RecordData

@JvmInline
public value class TXT(public val txt: List<String>): RecordData

@JvmInline
public value class A(public val a: String?): RecordData

@JvmInline
public value class AAAA(public val aaaa: String?): RecordData
