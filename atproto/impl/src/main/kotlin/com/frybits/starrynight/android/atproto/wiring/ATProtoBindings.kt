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

package com.frybits.starrynight.android.atproto.wiring

import com.frybits.starrynight.android.atproto.network.ATProtoNetworkApi
import com.frybits.starrynight.atproto.data.UserDao
import com.frybits.starrynight.atproto.data.UserDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import retrofit2.Retrofit
import retrofit2.create

@ContributesTo(AppScope::class)
@BindingContainer
public object ATProtoBindings {

    @Provides
    @SingleIn(AppScope::class)
    internal fun provideATProtoServicesApi(retrofit: Retrofit): ATProtoNetworkApi {
        return retrofit.create()
    }

    @Provides
    @SingleIn(AppScope::class)
    internal fun provideUseDao(appDatabase: UserDatabase): UserDao {
        return appDatabase.userDao()
    }
}
