package com.boswelja.devicemanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.wearable.activity.ConfirmationActivity
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.CapabilityApi
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.android.wearable.intent.RemoteIntent
import com.google.android.wearable.playstore.PlayStoreAvailability

class MainActivity : WearableActivity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        CapabilityApi.CapabilityListener {

    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var fragmentHolder: View
    private var node: Node? = null
    private var controlsFragmentActive = false

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo?) {
        node = capabilityInfo!!.nodes.lastOrNull()
        if (node != null) {
            showControlsFragment()
        } else {
            showConfirmationFragment()
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d("MainActivity", "Connection failed")
    }

    override fun onConnected(p0: Bundle?) {
        Wearable.CapabilityApi.addCapabilityListener(
                googleApiClient,
                this,
                Config.CAPABILITY_PHONE_APP)
                .setResultCallback {
                    if (it.isSuccess) {
                        val capabilityCallbacks = object: Utils.CapabilityCallbacks {
                            override fun capableDeviceFound(node: Node?) {
                                showControlsFragment()
                            }

                            override fun noCapableDevices() {
                                showConfirmationFragment()
                            }
                        }
                        Utils.isCompanionAppInstalled(googleApiClient, capabilityCallbacks)
                    } else {
                        Log.d("MainActivity", "Connection failed")
                    }
                }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d("MainActivity", "Google services disconnected")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentHolder = findViewById(R.id.fragment_holder)
        googleApiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
    }

    override fun onPause() {
        super.onPause()
        if (googleApiClient.isConnected) {
            Wearable.CapabilityApi.removeCapabilityListener(
                    googleApiClient,
                    this,
                    Config.CAPABILITY_PHONE_APP)
            googleApiClient.disconnect()
        }
    }

    override fun onResume() {
        super.onResume()
        googleApiClient.connect()
    }

    private fun showControlsFragment() {
        val fragmentTransaction = fragmentManager.beginTransaction()
        if (!controlsFragmentActive) {
            fragmentTransaction.replace(R.id.fragment_holder,
                    DeviceControlsFragment())
                    .commit()
            controlsFragmentActive = true
        }
    }

    private fun showConfirmationFragment() {
        val fragmentTransaction = fragmentManager.beginTransaction()
        val confirmationFragment = ConfirmationFragment()
        confirmationFragment.setHeaderText(getString(R.string.companion_app_missing))
        confirmationFragment.setDescText(getString(R.string.companion_app_missing_desc))
        confirmationFragment.setButtonCallback(object: ConfirmationFragment.ButtonCallbacks {
            override fun onAccept() {
                val intent = Intent(this@MainActivity, ConfirmationActivity::class.java)
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.OPEN_ON_PHONE_ANIMATION)
                Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback {
                    node = it.nodes.toSet().lastOrNull()
                    if (node != null) {
                        val playStoreIntent = Intent(Intent.ACTION_VIEW)
                        if (PlayStoreAvailability.getPlayStoreAvailabilityOnPhone(this@MainActivity) == PlayStoreAvailability.PLAY_STORE_ON_PHONE_AVAILABLE) {
                            playStoreIntent.data = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
                        } else {
                            playStoreIntent.data = Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                        }
                        playStoreIntent.addCategory(Intent.CATEGORY_BROWSABLE)
                        RemoteIntent.startRemoteActivity(this@MainActivity,
                                playStoreIntent,
                                null, node!!.id)
                        startActivity(intent)
                    }
                }
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
