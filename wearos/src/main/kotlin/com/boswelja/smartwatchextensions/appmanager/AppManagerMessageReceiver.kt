package com.boswelja.smartwatchextensions.appmanager

import com.boswelja.smartwatchextensions.common.appmanager.Messages.CHECK_CACHE_VALID
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_OPEN_PACKAGE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_UNINSTALL_PACKAGE
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class AppManagerMessageReceiver : WearableListenerService() {

    override fun onMessageReceived(event: MessageEvent) {
        when (event.path) {
            REQUEST_UNINSTALL_PACKAGE -> {
                event.data.toPackageName()?.also { packageName ->
                    requestUninstallPackage(packageName)
                }
            }
            REQUEST_OPEN_PACKAGE -> {
                event.data.toPackageName()?.also { packageName ->
                    openPackage(packageName)
                }
            }
            CHECK_CACHE_VALID -> {
                // TODO Validate cache and send a response
            }
        }
    }
}
