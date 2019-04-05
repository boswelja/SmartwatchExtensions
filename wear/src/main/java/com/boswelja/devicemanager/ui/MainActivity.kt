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
import com.google.android.gms.wearable.Node

class MainActivity : AppCompatActivity(), WearableNavigationDrawerView.OnItemSelectedListener {

    private lateinit var navigationDrawer: WearableNavigationDrawerView
    private val controlsFragment = ControlsFragment()
    private var settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigationDrawer = findViewById(R.id.navigation_drawer)
        navigationDrawer.setAdapter(NavigationDrawerAdapter(this))
        navigationDrawer.addOnItemSelectedListener(this)
        navigate(controlsFragment)
    }

    override fun onResume() {
        super.onResume()

        // Check for companion app
        val capabilityCallbacks = object : Utils.CapabilityCallbacks {
            override fun capableDeviceFound(node: Node?) {}

            override fun noCapableDevices() {
                val intent = Intent(this@MainActivity, ConfirmInstallActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        Utils.isCompanionAppInstalled(this, capabilityCallbacks)
    }

    private fun navigate(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.commit()
    }

    override fun onItemSelected(pos: Int) {
        val itemSection = NavigationDrawerSections.values()[pos]
        when (itemSection) {
            NavigationDrawerSections.Controls -> {
                navigate(controlsFragment)
            }
            NavigationDrawerSections.Settings -> {
                navigate(settingsFragment)
            }
        }
    }

}
