package com.boswelja.devicemanager.watchmanager.item

import androidx.room.Entity

@Entity(primaryKeys = ["id", "pref_key"], tableName = "int_preferences")
class IntPreference(watchId: String, key: String, value: Int) :
    Preference<Int>(watchId, key, value)
