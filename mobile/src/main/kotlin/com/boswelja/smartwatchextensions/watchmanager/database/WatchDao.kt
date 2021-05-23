package com.boswelja.smartwatchextensions.watchmanager.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchDao {
    @Query("SELECT * FROM watches WHERE id = :watchId LIMIT 1")
    fun get(watchId: UUID): Flow<DbWatch?>

    @Query("SELECT * FROM watches")
    fun getAll(): Flow<List<DbWatch>>

    @Query("UPDATE watches SET name = :name WHERE id = :watchId")
    suspend fun setName(watchId: UUID, name: String)

    @Insert
    suspend fun add(watch: DbWatch)

    @Query("DELETE FROM watches WHERE id = :watchId")
    suspend fun remove(watchId: UUID)
}
