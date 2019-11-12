package com.boswelja.devicemanager.watchconnectionmanager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IntPreferenceDao {

    @Query("SELECT * FROM int_preferences WHERE id = :id")
    fun getAllForWatch(id: String): Array<IntPreference>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(boolPreference: IntPreference)

    @Query("UPDATE int_preferences SET value = :newValue WHERE id = :id AND pref_key = :key")
    fun updateBoolPreference(id: String, key: String, newValue: Boolean)

    @Delete
    fun remove(boolPreference: IntPreference)

    @Query("DELETE FROM int_preferences WHERE id = :id")
    fun deleteAllForWatch(id: String)
}