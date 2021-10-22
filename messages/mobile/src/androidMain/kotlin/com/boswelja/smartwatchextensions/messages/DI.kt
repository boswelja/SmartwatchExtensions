package com.boswelja.smartwatchextensions.messages

import com.boswelja.smartwatchextensions.messages.database.MessagesDatabaseLoader
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val messagesModule = module {
    single<MessagesRepository> { MessagesDbRepository(get(), Dispatchers.IO) }
    single { MessagesDatabaseLoader(get()).createDatabase() }
}
