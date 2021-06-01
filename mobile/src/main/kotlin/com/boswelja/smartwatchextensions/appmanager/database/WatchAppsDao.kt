package com.boswelja.smartwatchextensions.appmanager.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boswelja.smartwatchextensions.appmanager.App
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchAppsDao {

    @Query("SELECT * FROM watch_apps WHERE watchId = :watchId")
    fun allForWatch(watchId: UUID): Flow<List<App>>

    @Query("SELECT COUNT(packageName) FROM watch_apps WHERE watchId = :watchId")
    fun countForWatch(watchId: UUID): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(app: App)

    @Query("DELETE FROM watch_apps WHERE watchId = :watchId")
    suspend fun removeForWatch(watchId: UUID)
}
