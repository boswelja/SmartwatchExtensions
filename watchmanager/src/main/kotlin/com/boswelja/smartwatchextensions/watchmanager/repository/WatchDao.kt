package com.boswelja.smartwatchextensions.watchmanager.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WatchDao {
    @Query("SELECT * FROM watches WHERE id = :watchId LIMIT 1")
    fun get(watchId: String): Flow<DbWatch?>

    @Query("SELECT * FROM watches")
    fun getAll(): Flow<List<DbWatch>>

    @Query("UPDATE watches SET name = :name WHERE id = :watchId")
    suspend fun setName(watchId: String, name: String)

    @Insert
    suspend fun add(watch: DbWatch)

    @Query("DELETE FROM watches WHERE id = :watchId")
    suspend fun remove(watchId: String)
}
