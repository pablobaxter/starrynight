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

package com.frybits.starrynight.android.di

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frybits.starrynight.android.BuildConfig
import com.frybits.starrynight.android.ui.splash.SplashKey
import com.frybits.starrynight.android.ui.splash.SplashScreen
import com.frybits.starrynight.utils.core.AppName
import com.frybits.starrynight.utils.core.ClientId
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
@BindingContainer
public object StarryNightBindings {

    @Provides
    @AppName
    internal val appName: String = BuildConfig.APP_NAME

    @Provides
    @ClientId
    internal val clientId: String = BuildConfig.CLIENT_ID

    @IntoSet
    @Provides
    public fun provideStarryEntryBuilder(): EntryProviderScope<NavKey>.() -> Unit = {
        entry<SplashKey> {
            SplashScreen()
        }
    }
}
