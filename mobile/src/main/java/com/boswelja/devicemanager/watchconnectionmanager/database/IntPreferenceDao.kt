/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchconnectionmanager.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boswelja.devicemanager.watchconnectionmanager.IntPreference

@Dao
interface IntPreferenceDao {

    @Query("SELECT * FROM int_preferences WHERE id = :id")
    fun getAllForWatch(id: String): Array<IntPreference>

    @Query("SELECT * FROM int_preferences WHERE id = :id AND pref_key = :key LIMIT 1")
    fun getWhere(id: String, key: String): IntPreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(intPreference: IntPreference)

    @Delete
    fun remove(intPreference: IntPreference)

    @Query("DELETE FROM int_preferences WHERE id = :id")
    fun deleteAllForWatch(id: String)
}
