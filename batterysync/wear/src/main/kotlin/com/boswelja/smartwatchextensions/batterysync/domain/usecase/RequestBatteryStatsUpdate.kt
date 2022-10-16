package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import android.content.Context
import com.boswelja.smartwatchextensions.batterysync.RequestBatteryStatus
import com.boswelja.smartwatchextensions.core.devicemanagement.phoneStateStore
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RequestBatteryStatsUpdate(
    context: Context,
) {
    private val messageClient = Wearable.getMessageClient(context)
    private val phoneStateStore = context.phoneStateStore

    suspend operator fun invoke(): Boolean {
        val phoneId = phoneStateStore.data.map { it.id }.first()
        return try {
            messageClient.sendMessage(
                phoneId,
                RequestBatteryStatus,
                null
            )
            true
        } catch (_: ApiException) {
            false
        }
    }
}
