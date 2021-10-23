package com.boswelja.smartwatchextensions.messages

import com.boswelja.smartwatchextensions.messages.ui.MessageHistoryViewModel
import com.boswelja.smartwatchextensions.messages.ui.MessagesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val messagesUiModule = module {
    viewModel { MessagesViewModel(get(), get()) }
    viewModel { MessageHistoryViewModel(get()) }
}
