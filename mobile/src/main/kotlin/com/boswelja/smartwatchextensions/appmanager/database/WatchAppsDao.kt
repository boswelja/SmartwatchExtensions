package com.boswelja.smartwatchextensions.appmanager.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchAppsDao {

    @Query("SELECT * FROM watch_apps WHERE watchId = :watchId")
    fun allForWatch(watchId: String): Flow<List<DbApp>>

    @Query("SELECT COUNT(packageName) FROM watch_apps WHERE watchId = :watchId")
    fun countForWatch(watchId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(app: DbApp)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(apps: List<DbApp>)

    @Delete
    suspend fun remove(app: DbApp)

    @Query("DELETE FROM watch_apps WHERE packageName = :packageName AND watchId = :watchId")
    suspend fun delete(watchId: String, packageName: String)

    @Query("DELETE FROM watch_apps WHERE watchId = :watchId")
    suspend fun removeForWatch(watchId: String)
}
