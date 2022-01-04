package com.boswelja.smartwatchextensions.batterysync

import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import com.boswelja.smartwatchextensions.devicemanagement.PhoneState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WearBatterySyncNotificationHelper(
    private val batterySyncStateRepository: BatterySyncStateRepository,
    private val phoneStateStore: DataStore<PhoneState>,
    context: Context,
    notificationManager: NotificationManager
) : AndroidBatterySyncNotificationHandler(context, notificationManager) {

    override suspend fun onNotificationPosted(targetUid: String) {
        batterySyncStateRepository.updateBatterySyncState {
            it.copy(notificationPosted = true)
        }
    }

    override suspend fun onNotificationCancelled(targetUid: String) {
        batterySyncStateRepository.updateBatterySyncState {
            it.copy(notificationPosted = false)
        }
    }

    override suspend fun getDeviceName(targetUid: String): String {
        return phoneStateStore.data
            .map { it.name }
            .first()
    }

    override suspend fun getChargeNotificationsEnabled(targetUid: String): Boolean {
        return batterySyncStateRepository.getBatterySyncState()
            .map { it.phoneChargeNotificationEnabled }
            .first()
    }

    override suspend fun getLowNotificationsEnabled(targetUid: String): Boolean {
        return batterySyncStateRepository.getBatterySyncState()
            .map { it.phoneLowNotificationEnabled }
            .first()
    }

    override suspend fun getChargeThreshold(targetUid: String): Int {
        return batterySyncStateRepository.getBatterySyncState()
            .map { it.phoneChargeThreshold }
            .first()
    }

    override suspend fun getLowThreshold(targetUid: String): Int {
        return batterySyncStateRepository.getBatterySyncState()
            .map { it.phoneLowThreshold }
            .first()
    }

    override suspend fun getNotificationAlreadySent(targetUid: String): Boolean {
        return batterySyncStateRepository.getBatterySyncState()
            .map { it.notificationPosted }
            .first()
    }
}
