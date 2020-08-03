/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.extensions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class ExtensionItem(
    val itemId: Int,
    @StringRes
    val textRes: Int,
    @DrawableRes
    val iconRes: Int,
    val enabled: Boolean = true,
    val extra: Int = -1
) {
    override fun equals(other: Any?): Boolean {
        return if (other is ExtensionItem) {
            other.itemId == this.itemId
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = itemId
        result = 31 * result + textRes
        result = 31 * result + iconRes
        result = 31 * result + enabled.hashCode()
        result = 31 * result + extra
        return result
    }
}
