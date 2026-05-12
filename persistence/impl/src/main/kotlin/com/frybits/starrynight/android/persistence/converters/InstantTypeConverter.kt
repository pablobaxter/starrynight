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

package com.frybits.starrynight.android.persistence.converters

import androidx.room.TypeConverter
import kotlin.time.Instant

internal class InstantTypeConverter {

    @TypeConverter
    fun fromTimeStamp(time: Long?): Instant? {
        return time?.let { Instant.fromEpochMilliseconds(time) }
    }

    @TypeConverter
    fun dateToTimeStamp(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }
}
