package com.boswelja.smartwatchextensions.messages

import com.boswelja.smartwatchextensions.messages.database.MessageDatabase
import com.boswelja.smartwatchextensions.messages.database.messagesDbAdapter
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val messagesModule = module {
    single<MessagesRepository> { MessagesDbRepository(get(), get(named("database"))) }
    single {
        MessageDatabase(
            get {
                parametersOf(MessageDatabase.Schema, "messages.db")
            },
            messagesDbAdapter
        )
    }
}
