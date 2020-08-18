/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.phonelocking.ui

import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.common.ui.BasePreferenceFragment
import com.boswelja.devicemanager.common.ui.BaseWatchPickerPreferenceActivity

class PhoneLockingPreferenceActivity : BaseWatchPickerPreferenceActivity() {
  override fun getWidgetFragment(): Fragment? = null
  override fun getPreferenceFragment(): BasePreferenceFragment = PhoneLockingPreferenceFragment()
}
