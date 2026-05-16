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
import androidx.room.Transaction
import com.frybits.starrynight.atproto.data.models.HandleRoomData
import com.frybits.starrynight.atproto.data.models.PdsRoomData
import com.frybits.starrynight.atproto.data.models.PlcRoomData
import kotlin.time.Instant

@Dao
public interface DidDao {

    @Query("SELECT * FROM PdsRoomData")
    public suspend fun getAllPdsData(): List<PdsRoomData>

    @Query("SELECT * FROM HandleRoomData")
    public suspend fun getAllHandleData(): List<HandleRoomData>

    @Query("SELECT * FROM HandleRoomData WHERE handle = :handle")
    public suspend fun getHandleData(handle: String): HandleRoomData

    @Query("SELECT * FROM PdsRoomData WHERE did = :did")
    public suspend fun getPdsData(did: String): PdsRoomData

    @Query("SELECT * FROM HandleRoomData WHERE did = :did")
    public suspend fun getHandlesForDid(did: String): List<HandleRoomData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertPdsData(vararg pdsData: PdsRoomData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insertHandleData(vararg handleData: HandleRoomData)

    @Delete
    public suspend fun deletePdsData(vararg pdsData: PdsRoomData)

    @Delete
    public suspend fun deleteHandleData(vararg handleData: HandleRoomData)

    @Query("DELETE FROM HandleRoomData WHERE did = :did")
    public suspend fun deleteHandlesForDid(did: String)

    @Query("DELETE FROM PdsRoomData")
    public suspend fun clearPdsData()

    @Query("DELETE FROM HandleRoomData")
    public suspend fun clearHandleData()

    @Transaction
    @Query("SELECT * FROM PdsRoomData WHERE did = :did")
    public suspend fun getPdsWithHandles(did: String): PlcRoomData

    @Transaction
    public suspend fun addHandleAndPdsData(did: String, handles: List<String>, pds: String, updateTime: Instant) {
        insertPdsData(PdsRoomData(did, pds, updateTime))
        deleteHandlesForDid(did)
        handles.forEach {
            insertHandleData(HandleRoomData(it, did, updateTime))
        }
    }
}
