package com.boswelja.smartwatchextensions.watchmanager.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boswelja.smartwatchextensions.watchmanager.item.BoolSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface BoolSettingDao {

    @Query("SELECT * FROM bool_preferences WHERE id = :watchId AND pref_key = :key LIMIT 1")
    fun get(watchId: String, key: String): Flow<BoolSetting?>

    @Query("SELECT * FROM bool_preferences WHERE id = :watchId")
    fun getByWatch(watchId: String): Flow<List<BoolSetting>>

    @Query("SELECT * FROM bool_preferences WHERE pref_key = :key")
    fun getByKey(key: String): Flow<List<BoolSetting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(boolPreference: BoolSetting)

    @Delete
    suspend fun remove(boolPreference: BoolSetting)

    @Query("DELETE FROM bool_preferences WHERE id = :watchId")
    suspend fun deleteAllForWatch(watchId: String)

    @Query("UPDATE bool_preferences SET value = :newValue WHERE pref_key = :key")
    suspend fun updateByKey(key: String, newValue: Boolean)
}
