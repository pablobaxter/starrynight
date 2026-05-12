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

package com.frybits.starrynight.android.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.frybits.starrynight.android.persistence.converters.InstantTypeConverter
import com.frybits.starrynight.atproto.data.ATProtoDatabase
import com.frybits.starrynight.atproto.data.models.ResolvedDid

@Database(
    entities = [
        ResolvedDid::class
    ], version = 1
)
@TypeConverters(
    InstantTypeConverter::class
)
internal abstract class AppDatabase :
    RoomDatabase(),
    ATProtoDatabase
