package com.boswelja.devicemanager.ui.batterysync

import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity

class BatterySyncPreferenceActivity : BasePreferenceActivity() {

    override fun createPreferenceFragment(): PreferenceFragmentCompat = BatterySyncPreferenceFragment()
    override fun createWidgetFragment(): Fragment? = BatterySyncPreferenceWidgetFragment()
}