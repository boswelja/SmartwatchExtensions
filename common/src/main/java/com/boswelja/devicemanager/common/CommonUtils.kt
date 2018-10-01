package com.boswelja.devicemanager.common

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import java.util.*

object CommonUtils {

    fun getUID(context: Context) : String {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        var uuid = sharedPrefs.getString(PreferenceKey.DEVICE_UID, "")!!
        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString(PreferenceKey.DEVICE_UID, uuid).apply()
        }
        return uuid
    }

    fun getUID(sharedPrefs: SharedPreferences) : String {
        var uuid = sharedPrefs.getString(PreferenceKey.DEVICE_UID, "")!!
        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString(PreferenceKey.DEVICE_UID, uuid).apply()
        }
        return uuid
    }

}