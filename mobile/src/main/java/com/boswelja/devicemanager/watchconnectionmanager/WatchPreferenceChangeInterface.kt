/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchconnectionmanager

interface WatchPreferenceChangeInterface {

    fun boolPreferenceChanged(boolPreference: BoolPreference)
    fun intPreferenceChanged(intPreference: IntPreference)
}
