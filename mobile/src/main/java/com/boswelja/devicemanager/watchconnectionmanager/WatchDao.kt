package com.boswelja.devicemanager.watchconnectionmanager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WatchDao {

    @Query("SELECT * FROM watches")
    fun getAll(): List<Watch>

    @Query("SELECT * FROM watches WHERE id = :id LIMIT 1")
    fun findById(id: String): Watch?

    @Insert
    fun add(watch: Watch)

    @Query("UPDATE watches SET has_app = :hasApp WHERE id = :id")
    fun setHasApp(id: String, hasApp: Boolean)

    @Delete
    fun remove(watch: Watch)
}