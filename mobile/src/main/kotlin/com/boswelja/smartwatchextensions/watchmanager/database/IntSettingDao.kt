package com.boswelja.smartwatchextensions.watchmanager.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boswelja.smartwatchextensions.watchmanager.item.IntSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface IntSettingDao {
    @Query("SELECT * FROM int_preferences WHERE id = :watchId AND pref_key = :key LIMIT 1")
    fun get(watchId: String, key: String): Flow<IntSetting?>

    @Query("SELECT * FROM int_preferences WHERE id = :watchId")
    fun getByWatch(watchId: String): Flow<List<IntSetting>>

    @Query("SELECT * FROM int_preferences WHERE pref_key = :key")
    fun getByKey(key: String): Flow<List<IntSetting>>

    @Query("DELETE FROM int_preferences WHERE pref_key = :key")
    suspend fun deleteByKey(key: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(intPreference: IntSetting)

    @Delete
    suspend fun remove(intPreference: IntSetting)

    @Query("DELETE FROM int_preferences WHERE id = :watchId")
    suspend fun deleteAllForWatch(watchId: String)
}
