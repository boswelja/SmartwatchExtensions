package com.boswelja.smartwatchextensions.watchmanager.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DbWatch::class], version = 2)
internal abstract class WatchDatabase : RoomDatabase() {

    internal abstract fun watchDao(): WatchDao

    companion object {
        fun create(context: Context): WatchDatabase {
            return Room.databaseBuilder(context, WatchDatabase::class.java, "watch-db")
                .apply {
                    fallbackToDestructiveMigration()
                }
                .build()
        }
    }
}
