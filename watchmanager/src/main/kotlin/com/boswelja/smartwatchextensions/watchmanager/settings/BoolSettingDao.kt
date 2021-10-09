package com.boswelja.smartwatchextensions.watchmanager.settings

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BoolSettingDao {

    @Query("SELECT * FROM bool_settings WHERE id = :watchId AND pref_key = :key LIMIT 1")
    fun get(watchId: String, key: String): Flow<DbBoolSetting?>

    @Query("SELECT * FROM bool_settings WHERE id = :watchId")
    fun getByWatch(watchId: String): Flow<List<DbBoolSetting>>

    @Query("SELECT * FROM bool_settings WHERE pref_key = :key")
    fun getByKey(key: String): Flow<List<DbBoolSetting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(boolPreference: DbBoolSetting)

    @Delete
    suspend fun remove(boolPreference: DbBoolSetting)

    @Query("DELETE FROM bool_settings WHERE id = :watchId")
    suspend fun deleteAllForWatch(watchId: String)

    @Query("UPDATE bool_settings SET value = :newValue WHERE pref_key = :key")
    suspend fun updateByKey(key: String, newValue: Boolean)
}
