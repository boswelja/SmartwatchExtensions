package com.boswelja.smartwatchextensions.batterysync.platform

import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatterySyncConfigRepository
import com.boswelja.smartwatchextensions.core.devicemanagement.PhoneState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * A [BatterySyncNotificationHandler] specifically for Wear.
 */
class WearBatterySyncNotificationHandler(
    private val batterySyncConfigRepository: BatterySyncConfigRepository,
    private val phoneStateStore: DataStore<PhoneState>,
    context: Context,
    notificationManager: NotificationManager
) : BatterySyncNotificationHandler(context, notificationManager) {

    override suspend fun onNotificationPosted(targetUid: String) {
        batterySyncConfigRepository.updateBatterySyncState {
            it.copy(notificationPosted = true)
        }
    }

    override suspend fun onNotificationCancelled(targetUid: String) {
        batterySyncConfigRepository.updateBatterySyncState {
            it.copy(notificationPosted = false)
        }
    }

    override suspend fun getDeviceName(targetUid: String): String {
        return phoneStateStore.data
            .map { it.name }
            .first()
    }

    override suspend fun getChargeNotificationsEnabled(targetUid: String): Boolean {
        return batterySyncConfigRepository.getBatterySyncState()
            .map { it.phoneChargeNotificationEnabled }
            .first()
    }

    override suspend fun getLowNotificationsEnabled(targetUid: String): Boolean {
        return batterySyncConfigRepository.getBatterySyncState()
            .map { it.phoneLowNotificationEnabled }
            .first()
    }

    override suspend fun getChargeThreshold(targetUid: String): Int {
        return batterySyncConfigRepository.getBatterySyncState()
            .map { it.phoneChargeThreshold }
            .first()
    }

    override suspend fun getLowThreshold(targetUid: String): Int {
        return batterySyncConfigRepository.getBatterySyncState()
            .map { it.phoneLowThreshold }
            .first()
    }

    override suspend fun getNotificationAlreadySent(targetUid: String): Boolean {
        return batterySyncConfigRepository.getBatterySyncState()
            .map { it.notificationPosted }
            .first()
    }
}
