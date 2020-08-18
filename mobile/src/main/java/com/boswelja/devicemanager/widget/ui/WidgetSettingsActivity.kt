/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.widget.ui

import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.common.ui.BasePreferenceActivity
import com.boswelja.devicemanager.common.ui.BasePreferenceFragment

class WidgetSettingsActivity : BasePreferenceActivity() {

  override fun getWidgetFragment(): Fragment? = WidgetSettingsWidget()
  override fun getPreferenceFragment(): BasePreferenceFragment = WidgetSettingsFragment()

  override fun onStop() {
    super.onStop()
    WatchBatteryWidget.updateWidgets(this)
  }
}
