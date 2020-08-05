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
import androidx.lifecycle.MutableLiveData

sealed class Item(
    val id: Int,
    @StringRes
    val textRes: Int
) {
    class Extension(
        id: Int,
        @StringRes
        textRes: Int,
        @StringRes
        val disabledTextRes: Int,
        @DrawableRes
        val iconRes: Int,
        enabled: Boolean = true,
        extra: Int = -1
    ) : Item(id, textRes) {

        val isEnabled = MutableLiveData(enabled)
        val extra = MutableLiveData(extra)

        override fun equals(other: Any?): Boolean {
            return if (other is Extension) {
                other.id == this.id
            } else {
                super.equals(other)
            }
        }

        override fun hashCode(): Int {
            var result = disabledTextRes
            result = 31 * result + iconRes
            result = 31 * result + isEnabled.hashCode()
            result = 31 * result + extra.hashCode()
            return result
        }
    }

    class Header(itemId: Int, @StringRes textRes: Int) : Item(itemId, textRes)
}
