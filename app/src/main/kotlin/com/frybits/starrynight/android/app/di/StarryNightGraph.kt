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

package com.frybits.starrynight.android.app.di

import android.app.Application
import com.frybits.starrynight.utils.core.AppName
import com.frybits.starrynight.utils.core.PackageName
import com.frybits.starrynight.utils.core.VersionCode
import com.frybits.starrynight.utils.core.VersionName
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.android.MetroAppComponentProviders

@DependencyGraph(AppScope::class)
internal interface StarryNightGraph: MetroAppComponentProviders {

    @DependencyGraph.Factory
    interface Factory {
        fun create(
            @Provides application: Application,
            @Provides @AppName appName: String,
            @Provides @PackageName packageName: String,
            @Provides @VersionName versionName: String,
            @Provides @VersionCode versionCode: Long
        ): StarryNightGraph
    }
}
