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
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.google.android.gms.wearable.Node

class MainActivity : AppCompatActivity() {

    private lateinit var fragmentHolder: View
    private var controlsFragmentActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentHolder = findViewById(R.id.fragment_holder)

        showControlsFragment()

        val capabilityCallbacks = object : Utils.CapabilityCallbacks {
            override fun capableDeviceFound(node: Node?) {}

            override fun noCapableDevices() {
                showInstallAppActivity()
            }
        }
        Utils.isCompanionAppInstalled(this, capabilityCallbacks)
    }

    private fun showControlsFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_holder,
                DeviceControlsFragment())
                .commit()
        controlsFragmentActive = true
    }

    private fun showInstallAppActivity() {
        val intent = Intent(this, ConfirmInstallActivity::class.java)
        startActivity(intent)
        finish()
    }
}
