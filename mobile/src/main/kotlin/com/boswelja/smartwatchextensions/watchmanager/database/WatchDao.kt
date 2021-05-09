package com.boswelja.smartwatchextensions.watchmanager.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID

@Dao
interface WatchDao {

    @Query("SELECT * FROM watches WHERE id = :watchId LIMIT 1")
    fun getObservable(watchId: UUID): LiveData<DbWatch?>

    @Query("SELECT * FROM watches WHERE id = :watchId LIMIT 1")
    suspend fun get(watchId: UUID): DbWatch?

    @Query("UPDATE watches SET name = :name WHERE id = :watchId")
    suspend fun setName(watchId: UUID, name: String)

    @Insert
    suspend fun add(watch: DbWatch)

    @Query("DELETE FROM watches WHERE id = :watchId")
    suspend fun remove(watchId: UUID)

    @Query("SELECT * FROM watches")
    fun getAllObservable(): LiveData<List<DbWatch>>

    @Query("SELECT * FROM watches")
    fun getAll(): List<DbWatch>

    @Query("SELECT * FROM watches WHERE platform = :platform AND platformId = :platformId LIMIT 1")
    suspend fun get(platform: String, platformId: String): DbWatch?
}
