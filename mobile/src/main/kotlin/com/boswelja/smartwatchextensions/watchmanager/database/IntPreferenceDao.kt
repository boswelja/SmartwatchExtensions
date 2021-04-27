package com.boswelja.smartwatchextensions.watchmanager.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boswelja.smartwatchextensions.watchmanager.item.IntPreference
import java.util.UUID

@Dao
interface IntPreferenceDao {

    @Query("SELECT * FROM int_preferences WHERE id = :watchId")
    fun getAllForWatch(watchId: UUID): List<IntPreference>

    @Query("SELECT * FROM int_preferences WHERE id = :watchId AND pref_key = :key LIMIT 1")
    fun get(watchId: UUID, key: String): IntPreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(intPreference: IntPreference)

    @Delete
    fun remove(intPreference: IntPreference)

    @Query("DELETE FROM int_preferences WHERE id = :watchId")
    fun deleteAllForWatch(watchId: UUID)

    @Query("SELECT * FROM int_preferences WHERE id = :watchId")
    fun getAllObservableForWatch(watchId: UUID): LiveData<Array<IntPreference>>

    @Query("SELECT * FROM int_preferences WHERE pref_key = :key")
    fun getAllObservableForKey(key: String): LiveData<Array<IntPreference>>

    @Query("SELECT * FROM int_preferences WHERE id = :watchId AND pref_key = :key LIMIT 1")
    fun getObservable(watchId: UUID, key: String): LiveData<IntPreference?>
}
