/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import com.boswelja.devicemanager.common.setup.References
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.google.android.gms.wearable.MessageClient

object Utils {

    /**
     * Registers a [Watch] with the given [WatchDatabase].
     * @param database The [WatchDatabase] to register the watch with.
     * @param messageClient A [MessageClient] to tell the watch it was registered with.
     * @param watch The [Watch] to register.
     * @return true if the watch was registered successfully, false otherwise.
     */
    fun addWatch(database: WatchDatabase, messageClient: MessageClient, watch: Watch): Boolean {
        if (database.addWatch(watch)) {
            messageClient.sendMessage(watch.id, References.WATCH_REGISTERED_PATH, null)
            return true
        }
        return false
    }
}
