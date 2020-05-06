package com.boswelja.devicemanager.ui.widget

import android.os.Bundle
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment

class WidgetSettingsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_widget_settings)
    }
}