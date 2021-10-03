package com.boswelja.smartwatchextensions.watchmanager.item

import androidx.room.Entity

@Entity(primaryKeys = ["id", "pref_key"], tableName = "bool_preferences")
class BoolSetting(watchId: String, key: String, value: Boolean) :
    Setting<Boolean>(watchId, key, value)
