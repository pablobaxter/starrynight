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

package com.frybits.gradle.atproto.utils

import java.util.Locale

internal fun String.camelToSnakeCase(): String {
    val regex = "(?<=[a-zA-Z])[A-Z]".toRegex()
    return regex.replace(this) { "_${it.value}" }.lowercase()
}

internal fun String.titleCaseFirstChar(): String {
    return replaceFirstChar { it.titlecase(Locale.US) }
}

internal fun String.lowerCaseFirstChar(): String {
    return replaceFirstChar { it.lowercase(Locale.US) }
}
