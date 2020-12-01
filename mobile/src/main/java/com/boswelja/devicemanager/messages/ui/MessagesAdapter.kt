package com.boswelja.devicemanager.messages.ui

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.databinding.MessageItemBinding
import com.boswelja.devicemanager.messages.Message
import com.boswelja.devicemanager.messages.MessageDiffCallback
import java.util.Date
import java.util.concurrent.TimeUnit

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
            holder.bind(message)
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

        /**
         * Convert millisecond time into a readable date string.
         * Shows time received if it's within the last 24 hours, otherwise shows the date.
         * @param timeInMillis The time milliseconds to convert to a readable string.
         */
        private fun getReceivedString(timeInMillis: Long): String {
            val todayMillis = System.currentTimeMillis()
            val received = Date(timeInMillis)
            val isToday = (todayMillis - timeInMillis) < TimeUnit.DAYS.toMillis(1)
            return if (isToday) {
                DateFormat.getTimeFormat(itemView.context).format(received)
            } else {
                DateFormat.getDateFormat(itemView.context).format(received)
            }
        }

        fun bind(message: Message) {
            binding.messageIcon.setImageResource(message.icon.iconRes)
            binding.messageTitle.text = message.title
            binding.messageText.text = message.text
            binding.receivedText.text = getReceivedString(message.timestamp)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MessageItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}
