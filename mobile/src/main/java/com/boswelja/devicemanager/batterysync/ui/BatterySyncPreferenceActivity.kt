/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.ui

import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.common.ui.BasePreferenceFragment
import com.boswelja.devicemanager.common.ui.BaseWatchPickerPreferenceActivity

class BatterySyncPreferenceActivity : BaseWatchPickerPreferenceActivity() {

    override fun getPreferenceFragment(): BasePreferenceFragment = BatterySyncPreferenceFragment()
    override fun getWidgetFragment(): Fragment = BatterySyncPreferenceWidgetFragment()
}
