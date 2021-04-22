package com.boswelja.smartwatchextensions.watchmanager.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.boswelja.watchconnection.core.Watch
import java.util.UUID

@Entity(tableName = "watches")
class DbWatch(
    @PrimaryKey override val id: UUID,
    name: String,
    platformId: String,
    platform: String
) : Watch(id, name, platformId, platform) {
    companion object {
        fun Watch.toDbWatch(): DbWatch {
            return DbWatch(id, name, platformId, platform)
        }
    }
}
