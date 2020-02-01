/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.messages

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
        @PrimaryKey val id: Int,
        val iconRes: Int,
        val labelRes: Int,
        val shortLabelRes: Int,
        val deleted: Boolean,
        val descRes: Int = 0,
        val buttonLabelRes: Int = 0) {
}
