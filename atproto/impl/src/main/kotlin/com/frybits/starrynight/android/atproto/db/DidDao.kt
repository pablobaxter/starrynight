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

package com.frybits.starrynight.android.atproto.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.frybits.starrynight.android.atproto.db.models.ResolvedDid

@Dao
public interface DidDao {

    @Query("SELECT * FROM ResolvedDid")
    public suspend fun getAll(): List<ResolvedDid>

    @Query("SELECT * FROM ResolvedDid WHERE did = :did")
    public suspend fun getAllResolvedHandlesForDid(did: String): List<ResolvedDid>

    @Query("SELECT * FROM ResolvedDid WHERE handle = :handle")
    public suspend fun getResolvedDidForHandle(handle: String): ResolvedDid?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertResolvedDid(vararg resolvedDid: ResolvedDid)

    @Delete
    public suspend fun deleteResolvedDid(vararg resolvedDid: ResolvedDid)

    @Query("DELETE FROM ResolvedDid")
    public suspend fun clearTable()
}
