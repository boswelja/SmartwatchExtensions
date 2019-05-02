package com.boswelja.devicemanager.ui.interruptfiltersync

import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment

class InterruptFilterSyncPreferenceActivity : BasePreferenceActivity() {

    override fun createPreferenceFragment(): BasePreferenceFragment = InterruptFilterSyncPreferenceFragment()
    override fun createWidgetFragment(): Fragment? = null
}