package com.boswelja.devicemanager.messages

import androidx.recyclerview.widget.DiffUtil

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }
}
