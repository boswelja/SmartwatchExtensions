package com.boswelja.devicemanager.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.boswelja.devicemanager.common.Config
import com.boswelja.devicemanager.receiver.DeviceAdminReceiver
import com.boswelja.devicemanager.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Wearable

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var TAG = "MainActivity"
    private var devicePolicyManager: DevicePolicyManager? = null
    var deviceAdminReceiver: ComponentName? = null
    private var settingsFragment: SettingsFragment? = null
    private lateinit var googleApiClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        devicePolicyManager =  getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        deviceAdminReceiver = DeviceAdminReceiver().getWho(this)

        settingsFragment = SettingsFragment()
        fragmentManager.beginTransaction().replace(R.id.fragment_holder, settingsFragment).commit()

        googleApiClient = GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Config.DEVICE_ADMIN_REQUEST_CODE -> {
                settingsFragment?.updatePrefSummary()
            }
        }
    }

    override fun onConnected(p0: Bundle?) {
        Log.d(TAG, "GoogleApiClient connected")
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d(TAG, "GoogleApiClient connection suspended")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d(TAG, "GoogleApiClient connection failed")
    }

    override fun onPause() {
        super.onPause()
        if (googleApiClient.isConnected) {
            googleApiClient.disconnect()
        }
    }

    override fun onResume() {
        super.onResume()
        googleApiClient.connect()
    }

    fun isDeviceAdmin(): Boolean {
        return devicePolicyManager!!.isAdminActive(deviceAdminReceiver)
    }

    fun changeAppIconVisibility(hide: Boolean) {
        val componentName = ComponentName(this, LauncherActivity::class.java)
        if (hide) {
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        } else {
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }
}
