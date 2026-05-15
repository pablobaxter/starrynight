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

package com.frybits.starrynight.atproto.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.frybits.starrynight.atproto.data.models.UserRoomData

@Dao
public interface UserDao {

    @Query("SELECT * FROM UserRoomData")
    public suspend fun getAll(): List<UserRoomData>

    @Query("SELECT * FROM UserRoomData WHERE handle = :handle")
    public suspend fun getUser(handle: String): UserRoomData

    @Query("SELECT * FROM UserRoomData WHERE did = :did")
    public suspend fun getUserByDid(did: String): UserRoomData

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertUser(vararg user: UserRoomData)

    @Delete
    public suspend fun deleteUser(vararg user: UserRoomData)

    @Query("DELETE FROM UserRoomData")
    public suspend fun clearTable()
}