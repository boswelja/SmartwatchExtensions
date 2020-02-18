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