package com.boswelja.devicemanager.watchmanager.item

import androidx.room.Entity

@Entity(primaryKeys = ["id", "pref_key"], tableName = "bool_preferences")
class BoolPreference(watchId: String, key: String, value: Boolean) :
    Preference<Boolean>(watchId, key, value)
