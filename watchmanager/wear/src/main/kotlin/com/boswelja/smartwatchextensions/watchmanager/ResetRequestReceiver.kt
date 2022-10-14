package com.boswelja.smartwatchextensions.watchmanager

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.getSystemService
import com.boswelja.smartwatchextensions.common.RequestResetApp
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage

class ResetRequestReceiver : MessageReceiver() {
    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        when (message.path) {
            RequestResetApp -> {
                val activityManager = context.getSystemService<ActivityManager>()
                activityManager?.clearApplicationUserData()
            }
        }
    }
}
