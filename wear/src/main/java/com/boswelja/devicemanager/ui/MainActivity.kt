/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.wear.widget.drawer.WearableNavigationDrawerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.ui.controls.ControlsFragment
import com.boswelja.devicemanager.ui.navigation.NavigationDrawerAdapter
import com.boswelja.devicemanager.ui.navigation.NavigationDrawerSections

class MainActivity : AppCompatActivity(), WearableNavigationDrawerView.OnItemSelectedListener {

    private val controlsFragment = ControlsFragment()
    private var settingsFragment: SettingsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        findViewById<WearableNavigationDrawerView>(R.id.navigation_drawer).apply {
            setAdapter(NavigationDrawerAdapter(this@MainActivity))
            addOnItemSelectedListener(this@MainActivity)
        }

        navigate(controlsFragment)

        // Check for companion app
        Utils.getCompanionNode(this)
                .addOnCompleteListener {
                    val node = it.result?.nodes?.lastOrNull()
                    if (node == null) {
                        showInstallMobileAppActivity()
                    }
                }
    }

    private fun showInstallMobileAppActivity() {
        val intent = Intent(this@MainActivity, InstallMobileAppActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigate(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .commit()
    }

    override fun onItemSelected(pos: Int) {
        when (NavigationDrawerSections.values()[pos]) {
            NavigationDrawerSections.Controls -> {
                navigate(controlsFragment)
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
