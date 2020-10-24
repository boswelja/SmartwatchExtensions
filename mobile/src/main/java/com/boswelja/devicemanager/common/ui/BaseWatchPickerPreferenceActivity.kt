/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.ActivitySettingsBinding
import timber.log.Timber

abstract class BaseWatchPickerPreferenceActivity : BaseWatchPickerActivity() {

  /** Get an instance of a class that extends [BasePreferenceFragment] here. Must not be null. */
  abstract fun getPreferenceFragment(): BasePreferenceFragment

  /**
   * Get an instance of a class that extends [Fragment] here to be used as a settings widget. Null
   * if you do not need a settings widget.
   */
  abstract fun getWidgetFragment(): Fragment?

  private lateinit var binding: ActivitySettingsBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySettingsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupWatchPickerSpinner(binding.toolbarLayout.toolbar, showUpButton = true)
    showFragments()
  }

  /**
   * Gets both preference fragment and widget fragment and shows them. If [getWidgetFragment]
   * returns null, it's ignored and the layout is cleaned up to remove it.
   */
  private fun showFragments() {
    Timber.d("showFragments() called")
    val preferenceFragment = getPreferenceFragment()
    val widgetFragment = getWidgetFragment()
    supportFragmentManager
        .beginTransaction()
        .apply {
          replace(R.id.fragment_holder, preferenceFragment)
          if (widgetFragment != null) {
            replace(R.id.widget_holder, widgetFragment)
          } else {
            Timber.i("No widget fragment to load")
            binding.widgetDivider.visibility = View.GONE
          }
        }
        .also { it.commitNow() }
  }

  companion object {
    const val EXTRA_PREFERENCE_KEY = "extra_preference_key"
  }
}
