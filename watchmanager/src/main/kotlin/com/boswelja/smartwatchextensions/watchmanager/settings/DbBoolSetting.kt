package com.boswelja.smartwatchextensions.watchmanager.settings

import androidx.room.Entity

@Entity(primaryKeys = ["id", "pref_key"], tableName = "bool_settings")
class DbBoolSetting(watchId: String, key: String, value: Boolean) :
    DbSetting<Boolean>(watchId, key, value)
