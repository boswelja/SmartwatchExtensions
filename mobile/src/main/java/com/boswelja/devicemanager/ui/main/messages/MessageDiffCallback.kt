package com.boswelja.devicemanager.ui.main.messages

import androidx.recyclerview.widget.DiffUtil
import com.boswelja.devicemanager.messages.Message

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id &&
                oldItem.label == newItem.label &&
                oldItem.timestamp == newItem.timestamp &&
                oldItem.deleted == newItem.deleted
    }

    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}