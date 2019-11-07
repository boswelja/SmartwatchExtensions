package com.boswelja.devicemanager.watchconnman

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WatchDao {

    @Query("SELECT * FROM watch")
    fun getAll(): List<Watch>

    @Query("SELECT * FROM watch WHERE id = :id LIMIT 1")
    fun findById(id: String): Watch?

    @Insert
    fun add(watch: Watch)

    @Query("UPDATE watch SET has_app = :hasApp WHERE id = :id")
    fun setHasApp(id: String, hasApp: Boolean)

    @Delete
    fun remove(watch: Watch)
}