package com.boswelja.devicemanager.watchmanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.boswelja.devicemanager.common.SingletonHolder
import com.boswelja.devicemanager.watchmanager.item.Watch

@Database(entities = [Watch::class], version = 8)
abstract class WatchDatabase : RoomDatabase() {

    internal abstract fun watchDao(): WatchDao

    fun renameWatch(watch: Watch, newName: String) = watchDao().setName(watch.id, newName)
    fun addWatch(watch: Watch) = watchDao().add(watch)
    fun removeWatch(watch: Watch) = watchDao().remove(watch.id)
    fun getAll() = watchDao().getAllObservable()
    fun getById(watchId: String) = watchDao().get(watchId)

    companion object : SingletonHolder<WatchDatabase, Context>({ context ->
        Room.databaseBuilder(context, WatchDatabase::class.java, "watch-db")
            .apply {
                addMigrations(
                    Migrations.MIGRATION_3_5,
                    Migrations.MIGRATION_4_5,
                    Migrations.MIGRATION_5_6,
                    Migrations.MIGRATION_6_7,
                    Migrations.MIGRATION_7_8
                )
                fallbackToDestructiveMigration()
            }
            .build()
    })
}
