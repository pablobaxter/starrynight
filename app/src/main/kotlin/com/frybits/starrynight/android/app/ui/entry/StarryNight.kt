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

package com.frybits.starrynight.android.app.ui.entry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.frybits.starrynight.android.app.ui.splash.SplashKey
import com.frybits.starrynight.home.HomeNavigation
import com.frybits.starrynight.login.LoginNavigation
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
internal fun StarryNight(
    viewModel: StarryNightViewModel = metroViewModel(),
    entryBuilders: Set<@JvmSuppressWildcards EntryProviderScope<NavKey>.() -> Unit>
) {
    val backStack = rememberNavBackStack(SplashKey)

    val currentState by viewModel.currentState.collectAsStateWithLifecycle()

    LaunchedEffect(currentState) {
        when (currentState) {
            is LoginState.Unknown -> Unit
            is LoginState.LoggedOut -> {
                backStack.clear()
                backStack.add(LoginNavigation.LoginKey)
            }
            is LoginState.LoggedIn -> {
                backStack.clear()
                backStack.add(HomeNavigation.HomeKey)
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entryBuilders.forEach { builder -> builder() }
        }
    )
}
