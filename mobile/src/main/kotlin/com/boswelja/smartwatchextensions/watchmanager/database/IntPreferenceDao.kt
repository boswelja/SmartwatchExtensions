package com.boswelja.smartwatchextensions.watchmanager.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boswelja.smartwatchextensions.watchmanager.item.IntPreference
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface IntPreferenceDao {
    @Query("SELECT * FROM int_preferences WHERE id = :watchId AND pref_key = :key LIMIT 1")
    fun get(watchId: UUID, key: String): Flow<IntPreference?>

    @Query("SELECT * FROM int_preferences WHERE id = :watchId")
    fun getByWatch(watchId: UUID): Flow<List<IntPreference>>

    @Query("SELECT * FROM int_preferences WHERE pref_key = :key")
    fun getByKey(key: String): Flow<List<IntPreference>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(intPreference: IntPreference)

    @Delete
    suspend fun remove(intPreference: IntPreference)

    @Query("DELETE FROM int_preferences WHERE id = :watchId")
    suspend fun deleteAllForWatch(watchId: UUID)
}
