package com.boswelja.smartwatchextensions.watchmanager.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.boswelja.watchconnection.core.Watch
import java.util.UUID

@Entity(tableName = "watches")
class DbWatch(
    @PrimaryKey override val id: UUID,
    name: String,
    @ColumnInfo(name = "platformId") override val internalId: String,
    platform: String
) : Watch(id, name, internalId, platform) {
    companion object {
        fun Watch.toDbWatch(): DbWatch {
            return DbWatch(id, name, internalId, platform)
        }
    }
}
