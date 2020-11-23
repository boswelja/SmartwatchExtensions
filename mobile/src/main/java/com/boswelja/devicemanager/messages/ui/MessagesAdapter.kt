package com.boswelja.devicemanager.messages.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.databinding.MessageItemBinding
import com.boswelja.devicemanager.messages.Message
import com.boswelja.devicemanager.messages.MessageDiffCallback

class MessagesAdapter(
    private val showAction: Boolean = true,
    private val actionButtonCallback: (action: Message.Action) -> Unit
) : PagingDataAdapter<Message, MessagesAdapter.ViewHolder>(
    MessageDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { message ->
            holder.binding.messageIcon.setImageResource(message.icon.iconRes)
            holder.binding.messageTitle.text = message.title
            holder.binding.messageText.text = message.text
            if (showAction && message.action != null) {
                holder.binding.messageActionButton.apply {
                    setText(message.action.labelRes)
                    setOnClickListener { actionButtonCallback(message.action) }
                    visibility = View.VISIBLE
                }
            }
        }
    }

    class ViewHolder(val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MessageItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}
