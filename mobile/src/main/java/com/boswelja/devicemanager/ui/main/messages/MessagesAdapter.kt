/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.messages

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.boswelja.devicemanager.messages.Message
import timber.log.Timber

class MessagesAdapter(private val clickCallback: (message: Message) -> Unit) :
    ListAdapter<Message, MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
        if (message.hasAction) {
            Timber.i("Setting up message action")
            holder.binding.messageActionButton.apply {
                setOnClickListener {
                    clickCallback(message)
                }
            }
        }
    }

    /**
     * Removes a [Message] from the list and updates the UI.
     * @param position The position of the [Message] to dismiss.
     */
    fun dismissMessage(position: Int) {
        currentList.removeAt(position)
        notifyItemRemoved(position)
    }
}
