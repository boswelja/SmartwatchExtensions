package com.boswelja.devicemanager.watchconnectionmanager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

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

    @Query("UPDATE watch SET bool_prefs = :boolPrefs WHERE id = :id")
    fun updateBoolPrefs(id: String, boolPrefs: HashMap<String, Boolean>)

    @Query("UPDATE watch SET int_prefs = :intPrefs WHERE id = :id")
    fun updateIntPrefs(id: String, intPrefs: HashMap<String, Int>)

    @Delete
    fun remove(watch: Watch)
}