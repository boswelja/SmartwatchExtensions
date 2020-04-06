/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.messages

import com.boswelja.devicemanager.messages.database.MessageDatabase

object MessageId {
    internal const val BATTERY_OPT_ENABLED = 0
    internal const val BATTERY_NOTIS_DISABLED = 1

    internal fun getGenericId(database: MessageDatabase): Int {
        var messageId = database.countMessages()
        while (database.messageExists(messageId)) {
            messageId++
        }
        return messageId
    }
}
