/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.wear.widget.drawer.WearableNavigationDrawerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.SettingsFragment
import com.boswelja.devicemanager.ui.navigation.NavigationDrawerAdapter
import com.boswelja.devicemanager.ui.navigation.NavigationDrawerSections

class MainActivity : AppCompatActivity(), WearableNavigationDrawerView.OnItemSelectedListener {

    private val mainFragment = MainFragment()
    private var settingsFragment: SettingsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        navigate(mainFragment)

        findViewById<WearableNavigationDrawerView>(R.id.navigation_drawer).apply {
            setAdapter(NavigationDrawerAdapter(this@MainActivity))
            addOnItemSelectedListener(this@MainActivity)
        }
    }

    private fun navigate(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .commit()
    }

    override fun onItemSelected(pos: Int) {
        when (NavigationDrawerSections.values()[pos]) {
            NavigationDrawerSections.Main -> {
                navigate(mainFragment)
            }
            NavigationDrawerSections.Settings -> {
                if (settingsFragment == null) {
                    settingsFragment = SettingsFragment()
                }
                navigate(settingsFragment!!)
            }
        }
    }
}
