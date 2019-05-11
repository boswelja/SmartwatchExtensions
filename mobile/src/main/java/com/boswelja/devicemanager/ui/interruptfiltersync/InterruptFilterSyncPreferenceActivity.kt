/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.interruptfiltersync

import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment

class InterruptFilterSyncPreferenceActivity : BasePreferenceActivity() {

    override fun createPreferenceFragment(): BasePreferenceFragment = InterruptFilterSyncPreferenceFragment()
    override fun createWidgetFragment(): Fragment? = null
}
