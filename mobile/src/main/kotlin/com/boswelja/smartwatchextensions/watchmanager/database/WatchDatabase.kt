package com.boswelja.smartwatchextensions.watchmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.boswelja.smartwatchextensions.common.RoomTypeConverters
import com.boswelja.smartwatchextensions.common.SingletonHolder
import com.boswelja.smartwatchextensions.watchmanager.database.DbWatch.Companion.toDbWatch
import com.boswelja.watchconnection.common.Watch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Database(entities = [DbWatch::class], version = 2)
@TypeConverters(RoomTypeConverters::class)
abstract class WatchDatabase : RoomDatabase() {

    internal abstract fun watchDao(): WatchDao

    suspend fun renameWatch(watch: Watch, newName: String) = watchDao().setName(watch.uid, newName)
    suspend fun addWatch(watch: Watch) = watchDao().add(watch.toDbWatch())
    suspend fun removeWatch(watch: Watch) = watchDao().remove(watch.uid)

    fun getById(watchId: String): Flow<Watch?> = watchDao().get(watchId)
        .map { it?.toWatch() }

    fun getAll(): Flow<List<Watch>> = watchDao().getAll()
        .map { watches -> watches.map { it.toWatch() } }

    companion object : SingletonHolder<WatchDatabase, Context>({ context ->
        Room.databaseBuilder(context, WatchDatabase::class.java, "watch-db")
            .apply {
                fallbackToDestructiveMigration()
            }
            .build()
    })
}
