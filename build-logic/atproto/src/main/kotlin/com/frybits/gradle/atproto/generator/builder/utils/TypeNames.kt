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

private const val STRING_PACKAGE_NAME = "com.frybits.starrynight.android.atproto.models.strings"

internal object TypeNames {

    val Retrofit = ClassName("retrofit2", "Retrofit")

    val RetrofitResponse = ClassName("retrofit2", "Response")

    val RetrofitBody = ClassName("retrofit2.http", "Body")

    val RetrofitQuery = ClassName("retrofit2.http", "Query")

    val RetrofitPost = ClassName("retrofit2.http", "POST")

    val RetrofitPath = ClassName("retrofit2.http", "Path")

    val RetrofitHeader = ClassName("retrofit2.http", "Header")

    val OkHttpResponseBody = ClassName("okhttp3", "ResponseBody")

    val OkHttpRequestBody = ClassName("okhttp3", "RequestBody")

    val ATIdentifier = ClassName(STRING_PACKAGE_NAME, "ATIdentifier")

    val DateTimeSerializer = ClassName("com.frybits.starrynight.atproto.serializers", "DateTimeSerializer")

    val Did = ClassName(STRING_PACKAGE_NAME, "Did")

    val Handle = ClassName(STRING_PACKAGE_NAME, "Handle")

    val Nsid = ClassName(STRING_PACKAGE_NAME, "Nsid")

    val Tid = ClassName(STRING_PACKAGE_NAME, "Tid")

    val RecordKey = ClassName(STRING_PACKAGE_NAME, "RecordKey")

    val URISerializer = ClassName("com.frybits.starrynight.atproto.serializers", "URISerializer")

    val Language = ClassName(STRING_PACKAGE_NAME, "Language")

    val BytesSerializer = ClassName("com.frybits.starrynight.atproto.serializers", "ATBytesSerializer")

    val Blob = ClassName(packageName = "com.frybits.starrynight.android.atproto.models.blob", "Blob")

    val StandardBlob = ClassName(packageName = "com.frybits.starrynight.android.atproto.models.blob", "StandardBlob")

    val EmptyBlob = ClassName(packageName = "com.frybits.starrynight.android.atproto.models.blob", "EmptyBlob")

    val ContributesTo = ClassName("dev.zacsweers.metro", "ContributesTo")

    val AppScope = ClassName("dev.zacsweers.metro", "AppScope")

    val BindingContainer = ClassName("dev.zacsweers.metro", "BindingContainer")

    val Provides = ClassName("dev.zacsweers.metro", "Provides")

    val SingleIn = ClassName("dev.zacsweers.metro", "SingleIn")
}
