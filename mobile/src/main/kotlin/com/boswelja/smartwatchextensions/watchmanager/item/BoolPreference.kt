package com.boswelja.smartwatchextensions.watchmanager.item

import androidx.room.Entity
import java.util.UUID

@Entity(primaryKeys = ["id", "pref_key"], tableName = "bool_preferences")
class BoolPreference(watchId: UUID, key: String, value: Boolean) :
    Preference<Boolean>(watchId, key, value)
