package com.boswelja.smartwatchextensions.batterysync

/**
 * An abstract class to handle Battery Sync Notification common logic.
 */
abstract class BaseBatterySyncNotificationHandler {

    /**
     * Get whether charge notifications are enabled for the device with the given UID.
     * @param targetUid The target device UID to check against.
     * @return true if charge notifications are enabled, false otherwise.
     */
    abstract suspend fun getChargeNotificationsEnabled(targetUid: String): Boolean

    /**
     * Get whether low notifications are enabled for the device with the given UID.
     * @param targetUid The target device UID to check against.
     * @return true if low notifications are enabled, false otherwise.
     */
    abstract suspend fun getLowNotificationsEnabled(targetUid: String): Boolean

    /**
     * Get the battery charge threshold for the device with the given UID.
     * @param targetUid The target device UID to check against.
     * @return The device battery charge threshold. Will be between 0 and 100.
     */
    abstract suspend fun getChargeThreshold(targetUid: String): Int

    /**
     * Get the battery low threshold for the device with the given UID.
     * @param targetUid The target device UID to check against.
     * @return The device battery low threshold. Will be between 0 and 100.
     */
    abstract suspend fun getLowThreshold(targetUid: String): Int

    /**
     * Get whether the user has been notified of a Battery Sync event for the device with the given
     * UID already.
     * @param targetUid The device UID to check against.
     * @return true if a notification has already been posted, false otherwise.
     */
    abstract suspend fun getNotificationAlreadySent(targetUid: String): Boolean

    internal abstract suspend fun cancelNotificationFor(targetUid: String)

    internal abstract suspend fun postChargeNotificationFor(targetUid: String, batteryStats: BatteryStats)

    internal abstract suspend fun postLowNotificationFor(targetUid: String, batteryStats: BatteryStats)

    /**
     * Posts a notification to the user if required, or cancels any existing invalid notifications.
     * @param targetUid The device UID to post notifications for.
     * @param batteryStats The device [BatteryStats] to post notifications for.
     */
    suspend fun handleNotificationsFor(targetUid: String, batteryStats: BatteryStats) {
        if (
            batteryStats.charging &&
            getChargeNotificationsEnabled(targetUid) &&
            batteryStats.percent >= getChargeThreshold(targetUid)
        ) {
            if (!getNotificationAlreadySent(targetUid)) {
                postChargeNotificationFor(targetUid, batteryStats)
            }
        } else if (
            !batteryStats.charging &&
            getLowNotificationsEnabled(targetUid) &&
            batteryStats.percent <= getLowThreshold(targetUid)
        ) {
            if (!getNotificationAlreadySent(targetUid)) {
                postLowNotificationFor(targetUid, batteryStats)
            }
        } else {
            cancelNotificationFor(targetUid)
        }
    }
}
