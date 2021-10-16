package com.boswelja.smartwatchextensions.messages

import com.boswelja.smartwatchextensions.messages.database.DB_DISPATCHER
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val messagesCommonModule = DI.Module(name = "MessagesCommon") {
    bind<MessagesRepository>() with singleton { MessagesDbRepository(instance(), DB_DISPATCHER) }
}
