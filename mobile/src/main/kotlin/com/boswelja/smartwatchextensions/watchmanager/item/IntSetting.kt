package com.boswelja.smartwatchextensions.watchmanager.item

import androidx.room.Entity
import java.util.UUID

@Entity(primaryKeys = ["id", "pref_key"], tableName = "int_preferences")
class IntSetting(watchId: String, key: String, value: Int) :
    Setting<Int>(watchId, key, value)
