package com.boswelja.smartwatchextensions.watchmanager.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boswelja.smartwatchextensions.watchmanager.item.BoolPreference
import java.util.UUID

@Dao
interface BoolPreferenceDao {

    @Query("SELECT * FROM bool_preferences WHERE id = :watchId")
    fun getAllForWatch(watchId: UUID): List<BoolPreference>

    @Query("SELECT * FROM bool_preferences WHERE pref_key = :key")
    fun getAllForKey(key: String): Array<BoolPreference>

    @Query("SELECT * FROM bool_preferences WHERE id = :watchId AND pref_key = :key LIMIT 1")
    fun get(watchId: UUID, key: String): BoolPreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun update(boolPreference: BoolPreference)

    @Delete
    fun remove(boolPreference: BoolPreference)

    @Query("DELETE FROM bool_preferences WHERE id = :watchId")
    fun deleteAllForWatch(watchId: UUID)

    @Query("SELECT * FROM bool_preferences WHERE id = :watchId")
    fun getAllObservableForWatch(watchId: UUID): LiveData<Array<BoolPreference>>

    @Query("SELECT * FROM bool_preferences WHERE pref_key = :key")
    fun getAllObservableForKey(key: String): LiveData<Array<BoolPreference>>

    @Query("SELECT * FROM bool_preferences WHERE id = :watchId AND pref_key = :key LIMIT 1")
    fun getObservable(watchId: UUID, key: String): LiveData<BoolPreference?>

    @Query("UPDATE bool_preferences SET value = :newValue WHERE pref_key = :key")
    fun updateAllForKey(key: String, newValue: Boolean)
}
