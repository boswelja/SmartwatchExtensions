/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.messages

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @DrawableRes val iconRes: Int,
    val label: String,
    val shortLabel: String,
    val deleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val desc: String = "",
    val buttonLabel: String = "",
    val action: Int = Action.NONE,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) {
    /**
     * Indicates whether the message has an action.
     */
    @Ignore val hasAction: Boolean = action != Action.NONE
}
