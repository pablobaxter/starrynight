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

package com.frybits.starrynight.android.app

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.frybits.starrynight.android.app.ui.StarryNightAppNav
import com.frybits.starrynight.android.app.ui.entry.StarryNight
import com.frybits.starrynight.android.app.ui.splash.SplashKey
import com.frybits.starrynight.android.theme.StarryNightTheme
import com.frybits.starrynight.login.LoginNavigation
import com.frybits.starrynight.navigation.LocalNavigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.android.ActivityKey
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory

@ContributesIntoMap(AppScope::class, binding<Activity>())
@ActivityKey
@Inject
internal class MainActivity(
    private val metroVmf: MetroViewModelFactory,
    private val entryBuilders: Set<@JvmSuppressWildcards EntryProviderScope<NavKey>.() -> Unit>,
) : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data
        val startKey = parseUri(uri)
        enableEdgeToEdge()
        setContent {
            val backStack = rememberNavBackStack(startKey)
            val starryNightNav = remember(backStack) { StarryNightAppNav(backStack) }
            CompositionLocalProvider(LocalMetroViewModelFactory provides metroVmf, LocalNavigator provides starryNightNav) {
                StarryNightTheme {
                    StarryNight(
                        entryBuilders = entryBuilders,
                        backStack = backStack
                    )
                }
            }
        }
    }

    private fun parseUri(uri: Uri?): NavKey {
        val path = uri?.path ?: return SplashKey
        return when {
            path.endsWith("login") -> LoginNavigation.LoginKey(uri.toString())
            else -> SplashKey
        }
    }
}
