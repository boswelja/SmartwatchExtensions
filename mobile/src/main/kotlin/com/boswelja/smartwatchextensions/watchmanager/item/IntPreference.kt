package com.boswelja.smartwatchextensions.watchmanager.item

import androidx.room.Entity
import java.util.UUID

@Entity(primaryKeys = ["id", "pref_key"], tableName = "int_preferences")
class IntPreference(watchId: UUID, key: String, value: Int) :
    Preference<Int>(watchId, key, value)
