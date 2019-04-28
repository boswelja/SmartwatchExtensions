/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.preference.confirmationdialog

import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat

class ConfirmationDialogPrefFragment : PreferenceDialogFragmentCompat() {

    private var key: String = ""

    override fun onDialogClosed(positiveResult: Boolean) {
        val pref = preference as ConfirmationDialogPreference
        if (positiveResult) {
            pref.setValue(!pref.getValue())
        }
    }

    companion object {
        fun newInstance(key: String): ConfirmationDialogPrefFragment {
            val frag = ConfirmationDialogPrefFragment()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            frag.arguments = b
            frag.key = key
            return frag
        }
    }
}