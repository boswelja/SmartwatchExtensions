/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boswelja.devicemanager.watchmanager.item.BoolPreference

@Dao
interface BoolPreferenceDao {

  @Query("SELECT * FROM bool_preferences WHERE id = :watchId")
  fun getAllForWatch(watchId: String): Array<BoolPreference>

  @Query("SELECT * FROM bool_preferences WHERE pref_key = :key")
  fun getAllForKey(key: String): Array<BoolPreference>

  @Query("SELECT * FROM bool_preferences WHERE id = :watchId AND pref_key = :key LIMIT 1")
  fun get(watchId: String, key: String): BoolPreference?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun update(boolPreference: BoolPreference)

  @Delete fun remove(boolPreference: BoolPreference)

  @Query("DELETE FROM bool_preferences WHERE id = :watchId")
  fun deleteAllForWatch(watchId: String)

  @Query("SELECT * FROM bool_preferences WHERE id = :watchId")
  fun getAllObservableForWatch(watchId: String): LiveData<Array<BoolPreference>>

  @Query("SELECT * FROM bool_preferences WHERE pref_key = :key")
  fun getAllObservableForKey(key: String): LiveData<Array<BoolPreference>>

  @Query("SELECT * FROM bool_preferences WHERE id = :watchId AND pref_key = :key LIMIT 1")
  fun getObservable(watchId: String, key: String): LiveData<BoolPreference?>
}
