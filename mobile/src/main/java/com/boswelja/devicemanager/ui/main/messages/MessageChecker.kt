/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.messages

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.batterysync.WatchBatteryUpdateReceiver
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey

object MessageChecker {

    private const val IGNORE_BATTERY_OPT_WARNING_KEY = "ignore_battery_opt_warning"
    private const val IGNORE_WATCH_CHARGE_NOTI_WARNING_KEY = "ignore_watch_charge_warning"

    fun countMessages(context: Context): Int {
        var count = 0
        if (shouldShowBatteryOptMessage(context)) count++
        if (shouldShowWatchChargeNotiMessage(context)) count++
        return count
    }

    fun shouldShowBatteryOptMessage(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val powerManager = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !sharedPreferences.getBoolean(IGNORE_BATTERY_OPT_WARNING_KEY, false) &&
                !powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun shouldShowWatchChargeNotiMessage(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return !sharedPreferences.getBoolean(IGNORE_WATCH_CHARGE_NOTI_WARNING_KEY, false) &&
                sharedPreferences.getBoolean(PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY, false) &&
                !Compat.areNotificationsEnabled(context, WatchBatteryUpdateReceiver.BATTERY_CHARGED_NOTI_CHANNEL_ID)
    }

    fun setIgnoreBatteryOpt(context: Context, ignoring: Boolean) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putBoolean(IGNORE_BATTERY_OPT_WARNING_KEY, ignoring).apply()
    }

    fun setIgnoreWatchCharge(context: Context, ignoring: Boolean) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putBoolean(IGNORE_WATCH_CHARGE_NOTI_WARNING_KEY, ignoring).apply()
    }
}
