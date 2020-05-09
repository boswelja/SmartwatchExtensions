/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.widget

import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import com.boswelja.devicemanager.ui.main.appsettings.AppSettingsFragment.Companion.SHOW_WIDGET_BACKGROUND_KEY
import com.boswelja.devicemanager.ui.main.appsettings.AppSettingsFragment.Companion.WIDGET_BACKGROUND_OPACITY_KEY

class WidgetSettingsActivity : BasePreferenceActivity() {

    private val widget = WidgetSettingsWidget()

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            SHOW_WIDGET_BACKGROUND_KEY -> {
                val hasBackground = sharedPreferences!!.getBoolean(key, true)
                widget.setWidgetBackgroundEnabled(hasBackground)
            }
            WIDGET_BACKGROUND_OPACITY_KEY -> {
                val backgroundOpacity = sharedPreferences!!.getInt(key, 60)
                widget.setWidgetBackgroundOpacity(backgroundOpacity)
            }
            else -> super.onSharedPreferenceChanged(sharedPreferences, key)
        }
    }

    override fun getWidgetFragment(): Fragment? = widget
    override fun getPreferenceFragment(): BasePreferenceFragment = WidgetSettingsFragment()

    override fun onStart() {
        super.onStart()
        widget.setWidgetBackgroundOpacity(sharedPreferences.getInt(WIDGET_BACKGROUND_OPACITY_KEY, 60))
        widget.setWidgetBackgroundEnabled(sharedPreferences.getBoolean(SHOW_WIDGET_BACKGROUND_KEY, true))
    }

    override fun onStop() {
        super.onStop()
        WatchBatteryWidget.updateWidgets(this)
    }
}
