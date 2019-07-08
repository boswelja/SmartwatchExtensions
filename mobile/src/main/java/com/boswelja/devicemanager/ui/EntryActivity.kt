package com.boswelja.devicemanager.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.References.CONNECTED_WATCH_ID_KEY
import com.boswelja.devicemanager.References.CONNECTED_WATCH_NAME_KEY
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.ui.main.MainActivity
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable

class EntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_entry_point)

        Wearable.getCapabilityClient(this)
                .getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE)
                .addOnSuccessListener {
                    if (it != null && !it.nodes.isNullOrEmpty()) {
                        val node = it.nodes.first { n -> n.isNearby }
                        PreferenceManager.getDefaultSharedPreferences(this)
                                .edit()
                                .putString(CONNECTED_WATCH_ID_KEY, node.id)
                                .putString(CONNECTED_WATCH_NAME_KEY, node.displayName)
                                .apply()
                        startMainActivity()
                    }
                }
    }

    private fun startMainActivity() {
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }
}