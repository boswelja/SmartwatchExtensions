package com.boswelja.smartwatchextensions.watchmanager.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.boswelja.watchconnection.common.Watch

@Entity(tableName = "watches")
internal data class DbWatch(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "platformId") val internalId: String,
    val platform: String
) {
    fun toWatch(): Watch {
        return Watch(
            id,
            name,
            internalId,
            platform
        )
    }

    companion object {
        fun Watch.toDbWatch(): DbWatch {
            return DbWatch(uid, name, internalId, platform)
        }
    }
}
