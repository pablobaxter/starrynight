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

import com.android.build.api.variant.DslLifecycle
import com.android.build.api.variant.Variant
import com.android.build.api.variant.VariantBuilder
import com.android.build.api.variant.VariantSelector
import org.gradle.api.Action

/**
 * This class wraps the [com.android.build.api.variant.AndroidComponentsExtension] class. The purpose for this is due to
 * AGP changing the generics of the [com.android.build.api.dsl.CommonExtension] class in AGP 8, and a binary breaking change in AGP 9.
 *
 * The only functions, variables, properties available for [com.android.build.api.variant.AndroidComponentsExtension] are the ones provided here.
 */
public interface ComponentsExtensionWrapper<DslExtensionT: CommonExtensionWrapper, VariantBuilderT: VariantBuilder, VariantT: Variant>: DslLifecycle<DslExtensionT> {

    /**
     * Creates a [VariantSelector] instance that can be configured to reduce the set of [com.android.build.api.variant.ComponentBuilder] instances participating in the
     * [beforeVariants] and [onVariants] callback invocation.
     *
     * @return [VariantSelector] to select the variants of interest.
     */
    public fun selector(): VariantSelector

    /**
     * Method to register a [callback] to be called with [VariantBuilderT] instances that satisfies the [selector]. The [callback] will be
     * called as soon as the [VariantBuilderT] instance has been created but before any [com.android.build.api.artifact.Artifact] has been
     * determined, therefore the build flow can still be changed when the [callback] is invoked.
     *
     * At this stage, access to the DSL objects is disallowed, use [finalizeDsl] method to programmatically access the DSL objects before the
     * [VariantBuilderT] object is built.
     *
     * The goal of this callback is to make changes before the variants and components are created. This includes enabling/disabling
     * components and making decisions on properties that can impact task creation and build flow. This guarantees that the matching
     * [onVariants] callback does not have to deal with changing configuration and can focus on updating task inputs. See [com.android.build.api.variant.ComponentBuilder]
     * for more information.
     *
     * Example without selection:
     * ```kotlin
     *  androidComponents {
     *      beforeVariants {
     *      }
     *  }
     * ```
     *
     * Example with selection:
     * ```kotlin
     *  androidComponents {
     *      val debug = selector().withBuildType("debug")
     *      beforeVariants(debug) {
     *      }
     *  }
     * ```
     *
     * See [here](https://developer.android.com/build/extend-agp#variant-api-artifacts-tasks) for more information
     *
     * @param selector [VariantSelector] to select which instance of [VariantBuilderT] are of interest. By default, all instances are of
     *   interest.
     * @param callback lambda to be called with each instance of [VariantBuilderT] of interest.
     */
    public fun beforeVariants(selector: VariantSelector = selector().all(), callback: (VariantBuilderT) -> Unit)

    /** [Action] based version of [beforeVariants] above. */
    public fun beforeVariants(selector: VariantSelector = selector().all(), callback: Action<VariantBuilderT>)

    /**
     * Allow for registration of a [callback] to be called with variant instances of type [VariantT] once the list of
     * [com.android.build.api.artifact.Artifact] has been determined.
     *
     * At this stage, access to the DSL objects is disallowed and access to the [VariantBuilderT] instance is limited to read-only access.
     *
     * At this stage, the build flow is final, with the callback for [beforeVariants] having modified properties that can impact which tasks
     * are created and how they are configured. The ability to query or modify the build intermediate files between tasks, including adding
     * additional new steps between existing tasks must be done via the [com.android.build.api.variant.Component.artifacts] API. See
     * [com.android.build.api.artifact.Artifacts] for details.
     *
     * The [VariantT] object exposes many [org.gradle.api.provider.Property] that are then used as task inputs. It is safe to set new values
     * on the properties, including using [org.gradle.api.provider.Provider] with or without task dependencies. When reading values, these
     * [org.gradle.api.provider.Property] must be lazily linked to properties used as task inputs. It is not safe to call
     * [org.gradle.api.provider.Property.get] during build configuration.
     *
     * Example without selection:
     * ```kotlin
     *  androidComponents {
     *      onVariants {
     *      }
     *  }
     * ```
     *
     * Example with selection:
     * ```kotlin
     *  androidComponents {
     *      val debug = selector().withBuildType("debug")
     *      onVariants(debug) {
     *      }
     *  }
     * ```
     *
     * See [here](https://developer.android.com/build/extend-agp#variant-api-artifacts-tasks) for more information
     *
     * @param selector [VariantSelector] to select which instance of [VariantBuilderT] are of interest. By default, all instances are of
     *   interest.
     * @param callback lambda to be called with each instance of [VariantT] of interest.
     */
    public fun onVariants(selector: VariantSelector = selector().all(), callback: (VariantT) -> Unit)

    /** [Action] based version of [onVariants] above. */
    public fun onVariants(selector: VariantSelector = selector().all(), callback: Action<VariantT>)
}
