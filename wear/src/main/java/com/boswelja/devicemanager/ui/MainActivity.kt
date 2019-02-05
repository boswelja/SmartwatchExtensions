/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.wearable.activity.ConfirmationActivity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.android.wearable.intent.RemoteIntent
import com.google.android.wearable.playstore.PlayStoreAvailability

class MainActivity : AppCompatActivity() {

    private lateinit var fragmentHolder: View
    private var controlsFragmentActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentHolder = findViewById(R.id.fragment_holder)

        Wearable
                .getCapabilityClient(this)
                .getCapability(
                        References.CAPABILITY_APP,
                        CapabilityClient.FILTER_REACHABLE
                )
                .addOnSuccessListener {
                    val capabilityCallbacks = object : Utils.CapabilityCallbacks {
                        override fun capableDeviceFound(node: Node?) {
                            showControlsFragment()
                        }

                        override fun noCapableDevices() {
                            showConfirmationFragment()
                        }
                    }
                    Utils.isCompanionAppInstalled(this, capabilityCallbacks)
                }
    }

    private fun showControlsFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (!controlsFragmentActive) {
            fragmentTransaction.replace(R.id.fragment_holder,
                    DeviceControlsFragment())
                    .commit()
            controlsFragmentActive = true
        }
    }

    private fun showConfirmationFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val confirmationFragment = ConfirmationFragment()
        confirmationFragment.setHeaderText(getString(R.string.companion_app_missing))
        confirmationFragment.setDescText(getString(R.string.companion_app_missing_desc))
        confirmationFragment.setButtonCallback(object : ConfirmationFragment.ButtonCallbacks {
            override fun onAccept() {
                val intent = Intent(this@MainActivity, ConfirmationActivity::class.java)
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.OPEN_ON_PHONE_ANIMATION)
                val capabilityCallback = object : Utils.CapabilityCallbacks {
                    override fun noCapableDevices() {
                        Toast.makeText(this@MainActivity, getString(R.string.no_device_paired), Toast.LENGTH_LONG).show()
                        this@MainActivity.finish()
                    }
                    override fun capableDeviceFound(node: Node?) {
                        val playStoreIntent = Intent(Intent.ACTION_VIEW)
                        if (PlayStoreAvailability.getPlayStoreAvailabilityOnPhone(this@MainActivity) == PlayStoreAvailability.PLAY_STORE_ON_PHONE_AVAILABLE) {
                            playStoreIntent.data = Uri.parse(String.format(getString(R.string.play_store_app_link), BuildConfig.APPLICATION_ID))
                        } else {
                            playStoreIntent.data = Uri.parse(String.format(getString(R.string.play_store_web_link), BuildConfig.APPLICATION_ID))
                        }
                        playStoreIntent.addCategory(Intent.CATEGORY_BROWSABLE)
                        RemoteIntent.startRemoteActivity(this@MainActivity,
                                playStoreIntent,
                                null, node!!.id)
                        startActivity(intent)
                    }
                }
                Utils.isCompanionAppInstalled(this@MainActivity, capabilityCallback)
            }

            override fun onCancel() {
                finish()
            }
        })
        fragmentTransaction.replace(R.id.fragment_holder,
                confirmationFragment)
                .commit()
        controlsFragmentActive = false
    }
}
