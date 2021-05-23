package com.boswelja.smartwatchextensions.watchmanager.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boswelja.smartwatchextensions.watchmanager.item.BoolPreference
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface BoolPreferenceDao {

    @Query("SELECT * FROM bool_preferences WHERE id = :watchId AND pref_key = :key LIMIT 1")
    fun get(watchId: UUID, key: String): Flow<BoolPreference?>

    @Query("SELECT * FROM bool_preferences WHERE id = :watchId")
    fun getByWatch(watchId: UUID): Flow<List<BoolPreference>>

    @Query("SELECT * FROM bool_preferences WHERE pref_key = :key")
    fun getByKey(key: String): Flow<List<BoolPreference>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(boolPreference: BoolPreference)

    @Delete
    suspend fun remove(boolPreference: BoolPreference)

    @Query("DELETE FROM bool_preferences WHERE id = :watchId")
    suspend fun deleteAllForWatch(watchId: UUID)

    @Query("UPDATE bool_preferences SET value = :newValue WHERE pref_key = :key")
    suspend fun updateByKey(key: String, newValue: Boolean)
}
