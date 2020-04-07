package com.boswelja.devicemanager.ui.phonelocking

import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment

class PhoneLockingPreferenceActivity : BasePreferenceActivity() {

    override fun createWidgetFragment(): Fragment? = null
    override fun createPreferenceFragment(): BasePreferenceFragment =
            PhoneLockingPreferenceFragment()

}