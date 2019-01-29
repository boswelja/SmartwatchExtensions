package com.boswelja.devicemanager.preference.confirmationdialog

import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat

class ConfirmationDialogPrefFragment : PreferenceDialogFragmentCompat() {

    private var key: String = ""

    override fun onDialogClosed(positiveResult: Boolean) {
        val pref = preference as ConfirmationDialogPreference
        if (pref.getValue() != positiveResult) {
            pref.sharedPreferences.edit().putBoolean(key, positiveResult).apply()
            pref.setValue(positiveResult)
            pref.onPreferenceChangeListener?.onPreferenceChange(pref, positiveResult)
        }

    }

    companion object {
        fun newInstance(key: String) : ConfirmationDialogPrefFragment {
            val frag = ConfirmationDialogPrefFragment()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            frag.arguments = b
            frag.key = key
            return frag
        }
    }
}