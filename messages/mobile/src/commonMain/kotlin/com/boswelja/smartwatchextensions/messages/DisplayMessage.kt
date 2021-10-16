package com.boswelja.smartwatchextensions.messages

data class DisplayMessage(
    val id: Long,
    val sourceUid: String?,
    val icon: Message.Icon,
    val title: String,
    val text: String,
    val action: Message.Action,
    val timestamp: Long
)
