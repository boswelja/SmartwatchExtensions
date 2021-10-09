package com.boswelja.smartwatchextensions.watchmanager.settings

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IntSettingDao {
    @Query("SELECT * FROM int_settings WHERE id = :watchId AND pref_key = :key LIMIT 1")
    fun get(watchId: String, key: String): Flow<DbIntSetting?>

    @Query("SELECT * FROM int_settings WHERE id = :watchId")
    fun getByWatch(watchId: String): Flow<List<DbIntSetting>>

    @Query("SELECT * FROM int_settings WHERE pref_key = :key")
    fun getByKey(key: String): Flow<List<DbIntSetting>>

    @Query("UPDATE int_settings SET value = :newValue WHERE pref_key = :key")
    suspend fun updateByKey(key: String, newValue: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(intPreference: DbIntSetting)

    @Delete
    suspend fun remove(intPreference: DbIntSetting)

    @Query("DELETE FROM int_settings WHERE id = :watchId")
    suspend fun deleteAllForWatch(watchId: String)
}
