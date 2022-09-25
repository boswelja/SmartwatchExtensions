package com.boswelja.smartwatchextensions.batterysync.domain.usecase

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.BatterySyncNotificationHandler
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.ui.batterywidget.WatchBatteryWidget

class StoreBatteryStatsForWatch(
    private val batteryStatsRepository: BatteryStatsRepository,
    private val batterySyncNotificationHandler: BatterySyncNotificationHandler,
    private val context: Context
) {
    suspend operator fun invoke(watchId: String, batteryStats: BatteryStats) {
        batteryStatsRepository.putBatteryStatsForWatch(watchId, batteryStats)
        batterySyncNotificationHandler.handleNotificationsFor(watchId, batteryStats)
        val widgetManager = GlanceAppWidgetManager(context)
        val widgetIds = widgetManager.getGlanceIds(WatchBatteryWidget::class.java)
        widgetIds.forEach { id ->
            updateAppWidgetState(context, id) {
                if (it[WatchBatteryWidget.watchIdKey] == watchId) {
                    it[WatchBatteryWidget.batteryPercentKey] = batteryStats.percent
                }
            }
            WatchBatteryWidget.update(context, id)
        }
    }
}
