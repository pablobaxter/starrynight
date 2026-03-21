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

package com.frybits.gradle.android.wrappers

/**
 * This class wraps the [com.android.build.api.dsl.CommonExtension] class. The purpose for this is due to
 * AGP changing the generics of this class in AGP 8, and a binary breaking change in AGP 9.
 *
 * The only functions, variables, properties available for [com.android.build.api.dsl.CommonExtension] are the ones provided here.
 */
public interface CommonExtensionWrapper {

    /**
     * The namespace of the generated R and BuildConfig classes. Also, the namespace used to resolve any relative class names that are
     * declared in the AndroidManifest.xml.
     */
    public var namespace: String?
}
