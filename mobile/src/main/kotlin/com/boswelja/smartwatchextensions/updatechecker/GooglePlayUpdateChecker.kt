package com.boswelja.smartwatchextensions.updatechecker

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo

/**
 * An [UpdateChecker] that checks Google Play for available updates.
 */
class GooglePlayUpdateChecker(context: Context) : UpdateChecker {

    private val updateManager = AppUpdateManagerFactory.create(context)

    override suspend fun isNewVersionAvailable(): Boolean {
        val updateInfo = updateManager.requestAppUpdateInfo()
        return updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
    }

    override fun launchDownloadScreen(context: Context) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=com.boswelja.smartwatchextensions")
        )
        context.startActivity(intent)
    }
}
