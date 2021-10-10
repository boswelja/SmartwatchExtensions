package com.boswelja.smartwatchextensions.bootorupdate

import android.content.Context

const val BOOT_OR_UPDATE_NOTI_CHANNEL_ID = "boot_or_update_noti_channel"
internal const val NOTI_ID = 69102

internal suspend fun Context.restartServices() {
// TODO Restore lookup by key functionality
//    WatchSettingsDatabase.getInstance(this).also {
//        tryStartInterruptFilterSyncService(it)
//        tryStartSeparationObserverService(it)
//    }
}
//
///** Try to start Do not Disturb change listener service if needed. */
//private suspend fun Context.tryStartInterruptFilterSyncService(repository: WatchSettingsRepository) {
//    withContext(Dispatchers.IO) {
//        val dndSyncToWatchEnabled =
//            repository.boolSettings().getByKey(DND_SYNC_TO_WATCH_KEY).first().any {
//                it.value
//            }
//        Timber.i(
//            "tryStartInterruptFilterSyncService dndSyncToWatchEnabled = $dndSyncToWatchEnabled"
//        )
//        if (dndSyncToWatchEnabled) {
//            ContextCompat.startForegroundService(
//                applicationContext,
//                Intent(applicationContext, DnDLocalChangeService::class.java)
//            )
//        }
//    }
//}
//
//private suspend fun Context.tryStartSeparationObserverService(database: WatchSettingsDatabase) {
//    val watchSeparationAlertsEnabled =
//        database.boolSettings().getByKey(WATCH_SEPARATION_NOTI_KEY).first().any { it.value }
//    if (watchSeparationAlertsEnabled) {
//        SeparationObserverService.start(this)
//    }
//}
