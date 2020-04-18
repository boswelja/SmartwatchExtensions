/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import timber.log.Timber

abstract class BasePreferenceActivity : BaseWatchPickerActivity() {

    private lateinit var preferenceFragment: BasePreferenceFragment

    /**
     * Create an instance of a class that extends [BasePreferenceFragment] here.
     * Must not be null.
     */
    abstract fun createPreferenceFragment(): BasePreferenceFragment

    /**
     * Create an instance of a class that extends [Fragment] here to be used as a settings widget.
     * Null if you do not need a settings widget.
     */
    abstract fun createWidgetFragment(): Fragment?

    override fun getContentViewId(): Int = R.layout.activity_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        showWidgetFragment()
        showPreferenceFragment()
    }

    /**
     * Creates an instance of a [BasePreferenceFragment] and shows it.
     */
    private fun showPreferenceFragment() {
        Timber.d("showPreferenceFragment() called")
        preferenceFragment = createPreferenceFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, preferenceFragment)
                .commit()
        if (intent != null && !intent.getStringExtra(EXTRA_PREFERENCE_KEY).isNullOrEmpty()) {
            val key = intent.getStringExtra(EXTRA_PREFERENCE_KEY)
            Timber.i("Scrolling to $key")
            preferenceFragment.scrollToPreference(key)
        }
    }

    /**
     * Tries to create and instance of [Fragment] and show it as a widget.
     * If the widget [Fragment] is null, fall back to hiding the space and divider.
     */
    private fun showWidgetFragment() {
        Timber.d("showWidgetFragment() called")
        val widgetFragment = createWidgetFragment()
        if (widgetFragment != null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.widget_holder, widgetFragment)
                    .commit()
        } else {
            Timber.i("No widget fragment to load")
            findViewById<View>(R.id.widget_divider).visibility = View.GONE
        }
    }

    companion object {
        const val EXTRA_PREFERENCE_KEY = "extra_preference_key"
    }
}
