package com.boswelja.smartwatchextensions.messages.database

import com.squareup.sqldelight.EnumColumnAdapter

internal val messagesDbAdapter = MessageDb.Adapter(
    iconAdapter = EnumColumnAdapter(),
    actionAdapter = EnumColumnAdapter()
)
