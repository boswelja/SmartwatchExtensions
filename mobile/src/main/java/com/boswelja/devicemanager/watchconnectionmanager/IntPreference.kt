package com.boswelja.devicemanager.watchconnectionmanager

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["id", "pref_key"], tableName = "int_preferences")
data class IntPreference(
        @ColumnInfo(name = "id") val watchId: String,
        @ColumnInfo(name = "pref_key") val key: String,
        @ColumnInfo(name = "value") val value: Int
)