package com.boswelja.devicemanager.watchconnectionmanager

interface WatchPreferenceChangeInterface {

    fun boolPreferenceChanged(boolPreference: BoolPreference)
    fun intPreferenceChanged(intPreference: IntPreference)

}