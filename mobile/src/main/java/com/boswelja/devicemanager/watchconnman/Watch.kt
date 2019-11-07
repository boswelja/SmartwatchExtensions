package com.boswelja.devicemanager.watchconnman

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Watch(
        @PrimaryKey val id: String,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "has_app") val hasApp: Boolean = false,
        @ColumnInfo(name = "int_prefs") val intPrefs: HashMap<String, Int> = HashMap(),
        @ColumnInfo(name = "bool_prefs") val boolPrefs: HashMap<String, Boolean> = HashMap()
)
