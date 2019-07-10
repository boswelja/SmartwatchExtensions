/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.References.CONNECTED_WATCH_ID_KEY
import com.boswelja.devicemanager.References.CONNECTED_WATCH_NAME_KEY
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.ui.main.MainActivity
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable

class EntryActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_entry_point)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        Wearable.getCapabilityClient(this)
                .getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE)
                .addOnSuccessListener {
                    if (it != null && !it.nodes.isNullOrEmpty()) {
                        val node = it.nodes.firstOrNull { n -> n.isNearby }
                        if (node != null) {
                            sharedPreferences.edit(commit = true) {
                                putString(CONNECTED_WATCH_ID_KEY, node.id)
                                putString(CONNECTED_WATCH_NAME_KEY, node.displayName)
                            }
                        } else {
                            setNoWatchesFound()
                        }
                    } else {
                        setNoWatchesFound()
                    }
                    startMainActivity()
                }
                .addOnFailureListener {
                    setNoWatchesFound()
                    startMainActivity()
                }
    }

    private fun setNoWatchesFound() {
        sharedPreferences.edit(commit = true) {
            putString(CONNECTED_WATCH_ID_KEY, "")
            putString(CONNECTED_WATCH_NAME_KEY, "")
        }
    }

    private fun startMainActivity() {
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }
}
