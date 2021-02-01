/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager.item

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["id", "pref_key"], tableName = "bool_preferences")
class BoolPreference(watchId: String, key: String, value: Boolean) :
    Preference<Boolean>(watchId, key, value)
