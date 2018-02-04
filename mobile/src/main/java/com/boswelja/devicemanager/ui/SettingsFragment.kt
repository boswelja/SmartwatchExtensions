package com.boswelja.devicemanager.ui

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import com.boswelja.devicemanager.common.Config
import com.boswelja.devicemanager.R

class SettingsFragment: PreferenceFragment(), Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private lateinit var mainActivity: MainActivity
    private var grantAdminPermPref: Preference? = null

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            Config.GRANT_PERMS_PREF_KEY -> {
                if (!mainActivity.isDeviceAdmin()) {
                    Utils.requestDeviceAdminPerms(mainActivity.deviceAdminReceiver)
                }
                true
            }
            else -> false
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (preference?.key) {
            Config.HIDE_APP_ICON_KEY -> {
                mainActivity.changeAppIconVisibility(newValue!! == true)
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = activity as MainActivity
        addPreferencesFromResource(R.xml.prefs)

        val hideAppIconPref = findPreference(Config.HIDE_APP_ICON_KEY)
        hideAppIconPref.onPreferenceChangeListener = this

        grantAdminPermPref = findPreference(Config.GRANT_PERMS_PREF_KEY)
        grantAdminPermPref?.onPreferenceClickListener = this
    }

    override fun onResume() {
        super.onResume()
        updatePrefSummary()
    }

    fun updatePrefSummary() {
        if (mainActivity.isDeviceAdmin()) {
            grantAdminPermPref?.summary = getString(R.string.pref_perms_granted)
        }
    }
}