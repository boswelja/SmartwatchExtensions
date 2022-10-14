package com.boswelja.smartwatchextensions.watchmanager

import android.content.Context
import android.content.pm.PackageManager
import com.boswelja.smartwatchextensions.common.RequestAppVersion
import com.boswelja.smartwatchextensions.common.Version
import com.boswelja.smartwatchextensions.common.VersionSerializer
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.common.message.MessageReceiver
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.wear.message.MessageClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VersionRequestReceiver : MessageReceiver(), KoinComponent {
    private val messageClient: MessageClient by inject()

    override suspend fun onMessageReceived(context: Context, message: ReceivedMessage<ByteArray?>) {
        if (message.path == RequestAppVersion) {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
            val version = Version(packageInfo.longVersionCode, packageInfo.versionName)
            messageClient.sendMessage(
                message.sourceUid,
                Message(
                    RequestAppVersion,
                    VersionSerializer.serialize(version)
                )
            )
        }
    }
}
