package com.boswelja.devicemanager.appmanager.ui.info

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.appmanager.Messages.REQUEST_OPEN_PACKAGE
import com.boswelja.devicemanager.common.appmanager.Messages.REQUEST_UNINSTALL_PACKAGE
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import java.text.SimpleDateFormat
import java.util.Locale

class AppInfoViewModel internal constructor(
    application: Application,
    private val messageClient: MessageClient
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) :
        this(application, Wearable.getMessageClient(application))

    private val dateFormatter = SimpleDateFormat("EE, dd MMM yyyy, h:mm aa", Locale.getDefault())

    var watchId: String? = null
    var app: App? = null

    fun formatDate(dateMillis: Long) = dateFormatter.format(dateMillis)

    /** Request uninstalling an app from the connected watch. */
    fun sendUninstallRequestMessage() {
        app?.packageName?.let {
            messageClient.sendMessage(
                watchId!!,
                REQUEST_UNINSTALL_PACKAGE,
                it.toByteArray(Charsets.UTF_8)
            )
        }
    }

    /** Request opening an app's launch activity on the connected watch. */
    fun sendOpenRequestMessage() {
        app?.packageName?.let {
            messageClient.sendMessage(
                watchId!!,
                REQUEST_OPEN_PACKAGE,
                it.toByteArray(Charsets.UTF_8)
            )
        }
    }
}
