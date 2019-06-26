/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.main.shortcuts.AppShortcutsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private val batterySyncFragment = BatterySyncFragment()
    private val lockPhoneFragment: LockPhoneFragment = LockPhoneFragment()
    private val appShortcutsFragment = AppShortcutsFragment()

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == PreferenceKey.BATTERY_SYNC_ENABLED_KEY) {
            recreateView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        recreateView()
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private fun addFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                    .add(R.id.content, fragment)
                    .commit()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Failed to add ${fragment::class} to the view")
            e.printStackTrace()
        }
    }

    private fun removeExistingFragments() {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
            remove(batterySyncFragment)
            remove(lockPhoneFragment)
            remove(appShortcutsFragment)
        }.also {
            try {
                it.commit()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to remove existing fragments from the view")
                e.printStackTrace()
            }
        }
    }

    private fun recreateView() {
        removeExistingFragments()
        if (!sharedPreferences.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false) or
                sharedPreferences.getBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)) {
            addFragment(lockPhoneFragment)
            addFragment(batterySyncFragment)
        } else {
            addFragment(batterySyncFragment)
            addFragment(lockPhoneFragment)
        }
        addFragment(appShortcutsFragment)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
