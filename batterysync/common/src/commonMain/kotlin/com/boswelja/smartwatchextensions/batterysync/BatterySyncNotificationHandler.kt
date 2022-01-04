package com.boswelja.smartwatchextensions.batterysync

expect abstract class BatterySyncNotificationHandler : BaseBatterySyncNotificationHandler {
    override suspend fun cancelNotificationFor(targetUid: String)
    override suspend fun postChargeNotificationFor(targetUid: String, batteryStats: BatteryStats)
    override suspend fun postLowNotificationFor(targetUid: String, batteryStats: BatteryStats)

    /**
     * Called when a notification is posted.
     * @param targetUid The UID of the device the notification belongs to.
     */
    abstract suspend fun onNotificationPosted(targetUid: String)

    /**
     * Called when a notification is cancelled.
     * @param targetUid The UID of the device the notification belonged to.
     */
    abstract suspend fun onNotificationCancelled(targetUid: String)

    /**
     * Get the name of the device with the given UID.
     * @param targetUid The device UID whose name to load.
     * @return The name of the device.
     */
    abstract suspend fun getDeviceName(targetUid: String): String

}