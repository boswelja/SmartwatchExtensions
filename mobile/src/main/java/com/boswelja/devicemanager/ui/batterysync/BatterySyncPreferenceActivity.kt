package com.boswelja.devicemanager.ui.batterysync

import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment

class BatterySyncPreferenceActivity : BasePreferenceActivity() {

    override fun createPreferenceFragment(): BasePreferenceFragment = BatterySyncPreferenceFragment()
    override fun createWidgetFragment(): Fragment? = BatterySyncPreferenceWidgetFragment()
}