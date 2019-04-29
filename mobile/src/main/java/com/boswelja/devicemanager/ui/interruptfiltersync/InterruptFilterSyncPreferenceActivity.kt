package com.boswelja.devicemanager.ui.interruptfiltersync

import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BasePreferenceActivity
import com.google.android.material.snackbar.Snackbar

class InterruptFilterSyncPreferenceActivity : BasePreferenceActivity() {

    override fun createPreferenceFragment(): PreferenceFragmentCompat {
        return InterruptFilterSyncPreferenceFragment()
    }

    override fun createWidgetFragment(): Fragment? {
        return null
    }

    fun createSnackbar(message: String) {
        Snackbar.make(findViewById(R.id.fragment_holder), message, Snackbar.LENGTH_LONG).show()
    }
}