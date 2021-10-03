package com.boswelja.smartwatchextensions.appmanager.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.boswelja.smartwatchextensions.common.RoomTypeConverters
import com.boswelja.smartwatchextensions.common.SingletonHolder

@Database(entities = [DbApp::class], version = 4)
@TypeConverters(RoomTypeConverters::class)
@AutoMigration(from = 3, to = 4)
abstract class WatchAppDatabase : RoomDatabase() {

    abstract fun apps(): WatchAppsDao

    companion object : SingletonHolder<WatchAppDatabase, Context>({
        Room.databaseBuilder(it, WatchAppDatabase::class.java, "watch-app-db")
            .fallbackToDestructiveMigration()
            .build()
    })
}
