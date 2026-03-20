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

package com.frybits.gradle.android

import com.android.build.api.dsl.AndroidResources
import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.Installation
import com.android.build.api.dsl.ProductFlavor
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantBuilder
import com.android.build.api.variant.VariantSelector
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

internal abstract class AGP8ComponentsExtensionWrapper @Inject internal constructor(
    private val project: Project,
    private val componentsExtension: AndroidComponentsExtension<CommonExtension<BuildFeatures, BuildType, DefaultConfig, ProductFlavor, AndroidResources, Installation>, VariantBuilder, Variant>
): ComponentsExtensionWrapper<AGP8CommonExtensionWrapper, VariantBuilder, Variant> {

    override fun selector(): VariantSelector = componentsExtension.selector()

    override fun beforeVariants(
        selector: VariantSelector,
        callback: (VariantBuilder) -> Unit
    ) = componentsExtension.beforeVariants(selector, callback)

    override fun beforeVariants(
        selector: VariantSelector,
        callback: Action<VariantBuilder>
    ) = componentsExtension.beforeVariants(selector, callback)

    override fun onVariants(
        selector: VariantSelector,
        callback: (Variant) -> Unit
    ) = componentsExtension.onVariants(selector, callback)

    override fun onVariants(
        selector: VariantSelector,
        callback: Action<Variant>
    ) = componentsExtension.onVariants(selector,callback)

    override fun finalizeDsl(callback: (AGP8CommonExtensionWrapper) -> Unit) {
        componentsExtension.finalizeDsl { commonExtension ->
            callback(project.objects.newInstance<AGP8CommonExtensionWrapper>(commonExtension))
        }
    }

    override fun finalizeDsl(callback: Action<AGP8CommonExtensionWrapper>) {
        componentsExtension.finalizeDsl { commonExtension ->
            callback.execute(project.objects.newInstance<AGP8CommonExtensionWrapper>(commonExtension))
        }
    }
}
