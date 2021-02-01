package com.boswelja.devicemanager.watchmanager.item

import androidx.room.ColumnInfo

abstract class Preference<T>(
    @ColumnInfo(name = "id")
    val watchId: String,
    @ColumnInfo(name = "pref_key")
    val key: String,
    @ColumnInfo(name = "value")
    val value: T
)
