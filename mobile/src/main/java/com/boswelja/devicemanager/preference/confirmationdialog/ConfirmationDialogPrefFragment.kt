/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.preference.confirmationdialog

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceDialogFragmentCompat
import timber.log.Timber

class ConfirmationDialogPrefFragment : PreferenceDialogFragmentCompat() {

  private var key: String = ""

  override fun onDialogClosed(positiveResult: Boolean) {
    Timber.i("onDialogClosed() called")
    val pref = preference as ConfirmationDialogPreference
    if (positiveResult) {
      pref.setValue(!pref.getValue())
    }
  }

  /** Shows the dialog fragment. */
  fun show(fragmentManager: FragmentManager) =
      show(fragmentManager, "ConfirmationDialogPrefFragment")

  companion object {
    /**
     * Creates a new instance of a [ConfirmationDialogPrefFragment].
     * @param key The preference key associated with the preference.
     * @return A new instance of [ConfirmationDialogPrefFragment].
     */
    fun newInstance(key: String): ConfirmationDialogPrefFragment {
      Timber.i("newInstance($key) called")
      val frag = ConfirmationDialogPrefFragment()
      val b = Bundle(1)
      b.putString(ARG_KEY, key)
      frag.arguments = b
      frag.key = key
      return frag
    }
  }
}
