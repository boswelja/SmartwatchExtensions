package com.boswelja.smartwatchextensions.watchmanager.settings

import androidx.room.Entity

@Entity(primaryKeys = ["id", "pref_key"], tableName = "int_settings")
class DbIntSetting(watchId: String, key: String, value: Int) :
    DbSetting<Int>(watchId, key, value)
