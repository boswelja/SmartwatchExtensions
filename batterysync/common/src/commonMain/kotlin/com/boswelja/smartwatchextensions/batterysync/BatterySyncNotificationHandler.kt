package com.boswelja.smartwatchextensions.batterysync

abstract class BatterySyncNotificationHandler {

    abstract suspend fun getChargeNotificationsEnabled(targetUid: String): Boolean

    abstract suspend fun getLowNotificationsEnabled(targetUid: String): Boolean

    abstract suspend fun getChargeThreshold(targetUid: String): Int

    abstract suspend fun getLowThreshold(targetUid: String): Int

    abstract suspend fun getNotificationAlreadySent(targetUid: String): Boolean

    internal abstract suspend fun cancelNotificationFor(targetUid: String)

    internal abstract suspend fun postChargeNotificationFor(targetUid: String, batteryStats: BatteryStats)

    internal abstract suspend fun postLowNotificationFor(targetUid: String, batteryStats: BatteryStats)

    suspend fun handleNotificationsFor(targetUid: String, batteryStats: BatteryStats) {
        if (batteryStats.charging && getChargeNotificationsEnabled(targetUid)) {
            if (batteryStats.percent >= getChargeThreshold(targetUid)) {
                postChargeNotificationFor(targetUid, batteryStats)
            }
        } else if (!batteryStats.charging && getLowNotificationsEnabled(targetUid)) {
            if (batteryStats.percent <= getLowThreshold(targetUid)) {
                postLowNotificationFor(targetUid, batteryStats)
            }
        } else {
            cancelNotificationFor(targetUid)
        }
    }
}
