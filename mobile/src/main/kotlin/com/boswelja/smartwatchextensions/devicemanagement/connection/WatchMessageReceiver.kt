package com.boswelja.smartwatchextensions.devicemanagement.connection

import android.content.Context
import android.content.Intent
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.core.devicemanagement.RequestLaunchApp
import com.boswelja.smartwatchextensions.main.ui.MainActivity
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage

/**
 * A [MessageReceiver] to handle messages received with no data.
 */
class WatchMessageReceiver : MessageReceiver() {

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        when (message.path) {
            RequestLaunchApp -> launchApp(context)
        }
    }

    /**
     * Launches Smartwatch Extensions to an activity containing a specified preference key.
     * @param context [Context].
     */
    private fun launchApp(context: Context) {
        context.startActivity<MainActivity>(flags = Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
