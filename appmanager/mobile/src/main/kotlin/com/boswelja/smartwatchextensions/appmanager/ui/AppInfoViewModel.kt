package com.boswelja.smartwatchextensions.appmanager.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.boswelja.smartwatchextensions.appmanager.PackageNameSerializer
import com.boswelja.smartwatchextensions.appmanager.RequestOpenPackage
import com.boswelja.smartwatchextensions.appmanager.WatchAppDetailsWithIcon
import com.boswelja.smartwatchextensions.appmanager.WatchAppIconRepository
import com.boswelja.smartwatchextensions.appmanager.WatchAppRepository
import com.boswelja.smartwatchextensions.core.watches.selected.SelectedWatchController
import com.boswelja.smartwatchextensions.wearable.ext.sendMessage
import com.google.android.gms.wearable.MessageClient
import kotlinx.coroutines.flow.first

/**
 * A ViewModel to provide data for App Info.
 */
class AppInfoViewModel(
    private val appRepository: WatchAppRepository,
    private val messageClient: MessageClient,
    private val selectedWatchController: SelectedWatchController,
    private val appIconRepository: WatchAppIconRepository
) : ViewModel() {

    /**
     * Requests the selected watch launch a given [WatchAppDetailsWithIcon].
     * @param app The [WatchAppDetailsWithIcon] to try launch.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun sendOpenRequest(app: WatchAppDetailsWithIcon): Boolean {
        return selectedWatchController.selectedWatch.first()?.let { watch ->
            messageClient.sendMessage(
                targetId = watch.uid,
                path = RequestOpenPackage,
                data = PackageNameSerializer.serialize(app.packageName),
            )
        } ?: false
    }

    /**
     * Requests the selected watch uninstall a given [WatchAppDetailsWithIcon].
     * @param app The [WatchAppDetailsWithIcon] to try uninstall.
     * @return true if the request was sent successfully, false otherwise.
     */
    suspend fun sendUninstallRequest(app: WatchAppDetailsWithIcon): Boolean {
        return selectedWatchController.selectedWatch.first()?.let { watch ->
            appRepository.delete(app.watchId, app.packageName)
            messageClient.sendMessage(
                targetId = watch.uid,
                path = RequestOpenPackage,
                data = PackageNameSerializer.serialize(app.packageName),

            )
        } ?: false
    }

    /**
     * Get more app details for a given watch app.
     */
    suspend fun getDetailsFor(packageName: String): WatchAppDetailsWithIcon? {
        return selectedWatchController.selectedWatch.first()?.let {
            val details = appRepository.getDetailsFor(it.uid, packageName).first()
            WatchAppDetailsWithIcon(details, loadIconOrNull(it.uid, details.packageName))
        }
    }

    /**
     * Load the icon for the given package, or null if no icon is available.
     */
    private suspend fun loadIconOrNull(watchId: String, packageName: String): Bitmap? {
        return appIconRepository.retrieveIconFor(watchId, packageName)?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
}
