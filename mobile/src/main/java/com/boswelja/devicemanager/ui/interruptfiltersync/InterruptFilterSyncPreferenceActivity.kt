package com.boswelja.devicemanager.ui.interruptfiltersync

import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity

class InterruptFilterSyncPreferenceActivity : BasePreferenceActivity() {

    override fun createPreferenceFragment(): PreferenceFragmentCompat = InterruptFilterSyncPreferenceFragment()
    override fun createWidgetFragment(): Fragment? = null
}