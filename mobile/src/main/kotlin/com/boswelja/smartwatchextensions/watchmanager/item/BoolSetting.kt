package com.boswelja.smartwatchextensions.watchmanager.item

import androidx.room.Entity
import java.util.UUID

@Entity(primaryKeys = ["id", "pref_key"], tableName = "bool_preferences")
class BoolSetting(watchId: UUID, key: String, value: Boolean) :
    Setting<Boolean>(watchId, key, value)
