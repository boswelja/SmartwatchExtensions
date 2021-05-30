package com.boswelja.smartwatchextensions.appmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.boswelja.smartwatchextensions.appmanager.App
import com.boswelja.smartwatchextensions.common.SingletonHolder

@Database(entities = [App::class], version = 1)
abstract class WatchAppDatabase : RoomDatabase() {

    abstract fun apps(): WatchAppsDao

    companion object : SingletonHolder<WatchAppDatabase, Context>({
        Room.databaseBuilder(it, WatchAppDatabase::class.java, "watch-app-db")
            .fallbackToDestructiveMigration()
            .build()
    })
}
