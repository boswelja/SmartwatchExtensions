package com.boswelja.devicemanager.phonelocking

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.boswelja.devicemanager.common.preference.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceAdminChangeReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Timber.i("onEnabled() called")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Timber.i("onDisabled() called")
        CoroutineScope(Dispatchers.getIO()).launch {
            WatchManager.getInstance(context).updatePreference(PHONE_LOCKING_ENABLED_KEY, false)
        }
    }
}
