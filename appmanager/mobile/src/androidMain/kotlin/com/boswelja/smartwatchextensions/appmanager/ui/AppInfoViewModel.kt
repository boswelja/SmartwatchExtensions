package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.lifecycle.ViewModel
import com.boswelja.smartwatchextensions.appmanager.PackageNameSerializer
import com.boswelja.smartwatchextensions.appmanager.REQUEST_OPEN_PACKAGE
import com.boswelja.smartwatchextensions.appmanager.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.smartwatchextensions.appmanager.WatchAppDetails
import com.boswelja.smartwatchextensions.appmanager.WatchAppRepository
import com.boswelja.smartwatchextensions.devicemanagement.SelectedWatchManager
import com.boswelja.watchconnection.common.message.Message
import com.boswelja.watchconnection.core.message.MessageClient
import com.boswelja.watchconnection.serialization.MessageHandler
import kotlinx.coroutines.flow.first

/**
 * A ViewModel to provide data for App Info.
 */
class AppInfoViewModel(
    private val appRepository: WatchAppRepository,
    private val messageClient: MessageClient,
    private val selectedWatchManager: SelectedWatchManager
) : ViewModel() {

    private val packageMessageHandler by lazy { MessageHandler(PackageNameSerializer, messageClient) }

    /**
     * Requests the selected watch launch a given [WatchAppDetails].
     * @param app The [WatchAppDetails] to try launch.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun sendOpenRequest(app: WatchAppDetails): Boolean {
        return selectedWatchManager.selectedWatch.first()?.let { watch ->
            packageMessageHandler.sendMessage(
                watch.uid,
                Message(
                    REQUEST_OPEN_PACKAGE,
                    app.packageName,
                    Message.Priority.HIGH
                )
            )
        } ?: false
    }

    /**
     * Requests the selected watch uninstall a given [WatchAppDetails].
     * @param app The [WatchAppDetails] to try uninstall.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun sendUninstallRequest(app: WatchAppDetails): Boolean {
        return selectedWatchManager.selectedWatch.first()?.let { watch ->
            appRepository.delete(app.watchId, app.packageName)
            packageMessageHandler.sendMessage(
                watch.uid,
                Message(
                    REQUEST_UNINSTALL_PACKAGE,
                    app.packageName,
                    Message.Priority.HIGH
                )
            )
        } ?: false
    }

    /**
     * Get more app details for a given watch app.
     */
    suspend fun getDetailsFor(packageName: String): WatchAppDetails? {
        return selectedWatchManager.selectedWatch.first()?.let {
            appRepository.getDetailsFor(it.uid, packageName).first()
        }
    }
}
