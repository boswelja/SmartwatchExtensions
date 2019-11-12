package com.boswelja.devicemanager.watchconnectionmanager

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["id", "pref_key"], tableName = "bool_preferences")
data class BoolPreference(
        @ColumnInfo(name = "id") val watchId: String,
        @ColumnInfo(name = "pref_key") val key: String,
        @ColumnInfo(name = "value") val value: Boolean
)