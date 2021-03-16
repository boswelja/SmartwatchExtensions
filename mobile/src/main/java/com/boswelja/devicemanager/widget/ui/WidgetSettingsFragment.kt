package com.boswelja.devicemanager.widget.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.boswelja.devicemanager.R

class WidgetSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_widget_settings)
    }
}
