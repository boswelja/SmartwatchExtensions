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
import com.boswelja.devicemanager.watchconnectionmanager.BoolPreference

@Dao
interface BoolPreferenceDao {

    @Query("SELECT * FROM bool_preferences WHERE id = :id")
    fun getAllForWatch(id: String): Array<BoolPreference>

    @Query("SELECT * FROM bool_preferences WHERE pref_key = :key")
    fun getAllForKey(key: String): Array<BoolPreference>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(boolPreference: BoolPreference)

    @Delete
    fun remove(boolPreference: BoolPreference)

    @Query("DELETE FROM bool_preferences WHERE id = :id")
    fun deleteAllForWatch(id: String)
}
