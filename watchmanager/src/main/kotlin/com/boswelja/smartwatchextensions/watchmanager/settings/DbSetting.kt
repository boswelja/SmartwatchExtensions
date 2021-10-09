package com.boswelja.smartwatchextensions.watchmanager.settings

import androidx.room.ColumnInfo

/**
 * An abstract class for storing preferences inside a Room database.
 */
abstract class DbSetting<T>(
    @ColumnInfo(name = "id")
    val watchId: String,
    @ColumnInfo(name = "pref_key")
    val key: String,
    @ColumnInfo(name = "value")
    val value: T
)
