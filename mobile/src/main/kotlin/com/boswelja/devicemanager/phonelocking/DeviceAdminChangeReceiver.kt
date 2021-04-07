package com.boswelja.devicemanager.phonelocking

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class DeviceAdminChangeReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Timber.i("onEnabled() called")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Timber.i("onDisabled() called")
        // TODO Tell registered watches Phone Locking was disabled.
    }
}
