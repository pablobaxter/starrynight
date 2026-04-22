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

package com.frybits.gradle.atproto.generator.builder.utils

import com.squareup.kotlinpoet.ClassName

private const val STRING_PACKAGE_NAME = "com.frybits.starrynight.atproto.models.strings"

internal object TypeNames {
    val RetrofitResponse = ClassName("retrofit2", "Response")

    val ATIdentifier = ClassName(STRING_PACKAGE_NAME, "ATIdentifier")

    val DateTimeSerializer = ClassName("com.frybits.starrynight.atproto.serializers", "DateTimeSerializer")

    val Did = ClassName(STRING_PACKAGE_NAME, "Did")

    val Handle = ClassName(STRING_PACKAGE_NAME, "Handle")

    val Nsid = ClassName(STRING_PACKAGE_NAME, "Nsid")

    val Tid = ClassName(STRING_PACKAGE_NAME, "Tid")

    val RecordKey = ClassName(STRING_PACKAGE_NAME, "RecordKey")

    val URISerializer = ClassName("com.frybits.starrynight.atproto.serializers", "URISerializer")

    val Language = ClassName(STRING_PACKAGE_NAME, "Language")
}
