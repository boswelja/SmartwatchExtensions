package com.boswelja.devicemanager.ui.main.messages

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.preference.PreferenceManager

object MessageChecker {

    private const val IGNORE_BATTERY_OPT_WARNING_KEY = "ignore_battery_opt_warning"

    fun countMessages(context: Context): Int {
        var count = 0
        if (shouldShowBatteryOptMessage(context)) count++
        return count
    }

    fun shouldShowBatteryOptMessage(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val powerManager = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !sharedPreferences.getBoolean(IGNORE_BATTERY_OPT_WARNING_KEY, false) &&
                !powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun setIgnoreBatteryOpt(context: Context, ignoring: Boolean) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putBoolean(IGNORE_BATTERY_OPT_WARNING_KEY, ignoring).apply()
    }
}