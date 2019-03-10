/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

class MainOption(val iconRes: Int, val label: String, val type: Type) {

    enum class Type {
        LOCK_PHONE,
        PHONE_BATTERY
    }

}