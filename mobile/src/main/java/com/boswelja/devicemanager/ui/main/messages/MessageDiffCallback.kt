/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
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
