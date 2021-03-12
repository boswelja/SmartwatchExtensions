package com.boswelja.devicemanager.dndsync.ui

import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.common.ui.activity.BasePreferenceFragment
import com.boswelja.devicemanager.common.ui.activity.BaseWatchPickerPreferenceActivity

class DnDSyncPreferenceActivity : BaseWatchPickerPreferenceActivity() {

    override fun getPreferenceFragment(): BasePreferenceFragment = DnDSyncPreferenceFragment()
    override fun getWidgetFragment(): Fragment = DnDSyncPreferenceWidgetFragment()
}
