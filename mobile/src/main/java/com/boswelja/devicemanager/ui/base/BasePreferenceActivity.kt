/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.base

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R

abstract class BasePreferenceActivity : BaseToolbarActivity() {

    lateinit var sharedPreferences: SharedPreferences

    private lateinit var preferenceFragment: BasePreferenceFragment

    abstract fun createPreferenceFragment(): BasePreferenceFragment
    abstract fun createWidgetFragment(): Fragment?

    override fun getContentViewId(): Int = R.layout.activity_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        preferenceFragment = createPreferenceFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, preferenceFragment)
                .commit()

        val widgetFragment = createWidgetFragment()
        if (widgetFragment != null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.widget_holder, widgetFragment)
                    .commit()
        } else {
            findViewById<View>(R.id.widget_divider).visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (intent != null && !intent.getStringExtra(EXTRA_PREFERENCE_KEY).isNullOrEmpty()) {
            val key = intent.getStringExtra(EXTRA_PREFERENCE_KEY)
            preferenceFragment.scrollToPreference(key)
        }
    }
}
