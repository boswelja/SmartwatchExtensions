package com.boswelja.smartwatchextensions.messages

import com.boswelja.smartwatchextensions.messages.database.MessageDatabase
import com.boswelja.smartwatchextensions.messages.database.MessagesDatabaseLoader
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val messagesModule = DI.Module(name = "Messages") {
    import(messagesCommonModule)
    bind<MessageDatabase>() with singleton { MessagesDatabaseLoader(instance()).createDatabase() }
}
