package com.boswelja.smartwatchextensions.devicemanagement.connection

import android.content.Intent
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.core.devicemanagement.RequestLaunchApp
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

/**
 * A [WearableListenerService] to handle messages received with no data.
 */
class WatchMessageReceiver : WearableListenerService() {

    override fun onMessageReceived(message: MessageEvent) {
        when (message.path) {
            RequestLaunchApp -> launchApp()
        }
    }

    /**
     * Launches Smartwatch Extensions to an activity containing a specified preference key.
     */
    private fun launchApp() {
        startActivity<MainActivity>(flags = Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
