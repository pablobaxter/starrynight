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

package com.frybits.starrynight.android.network.wiring

import android.app.Application
import android.net.DnsResolver
import com.frybits.starrynight.utils.core.AppName
import com.frybits.starrynight.utils.core.PackageName
import com.frybits.starrynight.utils.core.VersionCode
import com.frybits.starrynight.utils.core.VersionName
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

@ContributesTo(AppScope::class)
@BindingContainer
public object CoreNetworkBindings {

    @Provides
    @SingleIn(AppScope::class)
    public fun provideDnsResolver(application: Application): DnsResolver {
        return DnsResolver.getInstance()
    }

    @Provides
    @SingleIn(AppScope::class)
    public fun provideJson(): Json {
        return Json
    }

    @Provides
    @SingleIn(AppScope::class)
    public fun provideOkHttp(
        @AppName appName: String,
        @PackageName packageName: String,
        @VersionName versionName: String,
        @VersionCode versionCode: Long
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "$packageName/$appName/$versionName($versionCode)")
                        .build()
                )
            }
            .build()
    }

    @Provides
    @SingleIn(AppScope::class)
    public fun provideRetrofitClient(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://frybits.com")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
