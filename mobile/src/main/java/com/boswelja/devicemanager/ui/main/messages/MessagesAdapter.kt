package com.boswelja.devicemanager.ui.main.messages

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Utils
import com.google.android.material.button.MaterialButton

internal class MessagesAdapter(private val fragment: MessageFragment) :
        RecyclerView.Adapter<MessagesAdapter.MessageItemViewHolder>() {

    private val messages = ArrayList<Message>()

    override fun getItemCount(): Int = messages.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageItemViewHolder =
            MessageItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_messages_item, parent, false))

    override fun onBindViewHolder(holder: MessageItemViewHolder, position: Int) {
        val message = messages[position]

        holder.iconView.setImageResource(message.iconRes)
        holder.labelView.setText(message.labelRes)
        if (message.descRes != 0) {
            holder.descView.setText(message.descRes)
        }
        if (message.buttonLabelRes != 0) {
            holder.dividerView.visibility = View.VISIBLE
            holder.expandedActionButton.visibility = View.VISIBLE
            holder.expandedActionButton.apply {
                setText(message.buttonLabelRes)
                setOnClickListener {
                    handleMessageActionClick(holder, message)
                }
            }
        }
        holder.itemView.setOnClickListener {
            holder.toggleExpanded()
            notifyItemChanged(holder.adapterPosition)
        }
    }

    @SuppressLint("BatteryLife")
    private fun handleMessageActionClick(holder: MessageItemViewHolder, message: Message) {
        when (message) {
            Message.BatteryOptWarning -> {
                val context = holder.itemView.context
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context?.packageName}")
                    }.also {
                        context.startActivity(it)
                    }

                }
            }
        }
    }

    fun notifyMessage(message: Message) {
        if (!messages.contains(message)) {
            messages.add(message)
            notifyItemInserted(messages.indexOf(message))
        }
    }

    fun dismissMessage(message: Message) =
            dismissMessage(message, false)

    internal fun dismissMessage(position: Int, allowUndo: Boolean) =
            dismissMessage(messages[position], allowUndo)

    private fun dismissMessage(message: Message, allowUndo: Boolean) {
        if (messages.contains(message)) {
            messages.indexOf(message).also {
                messages.removeAt(it)
                notifyItemRemoved(it)
            }
        }
        if (allowUndo) fragment.dismissMessage(message)
    }

    class MessageItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val iconView: AppCompatImageView = itemView.findViewById(R.id.message_icon)
        val labelView: AppCompatTextView = itemView.findViewById(R.id.message_label)
        val descView: AppCompatTextView = itemView.findViewById(R.id.message_desc)
        val expandedActionButton: MaterialButton = itemView.findViewById(R.id.message_action_button)
        val dividerView: View = itemView.findViewById(R.id.divider)

        private val expandedIndicatorView: AppCompatImageView = itemView.findViewById(R.id.message_expanded_indicator)
        private val expandedContentsHolder: View = itemView.findViewById(R.id.message_expanded_content_holder)

        private var isExpanded = false

        fun toggleExpanded() {
            isExpanded = !isExpanded
            Log.d("MessagesAdapter", "toggleExpanded: $isExpanded")
            val itemViewPadding = Utils.complexTypeDp(itemView.resources, 8f).toInt()
            if (isExpanded) {
                labelView.maxLines = 2
                expandedIndicatorView.setImageResource(R.drawable.ic_expand_less)
                expandedContentsHolder.visibility = View.VISIBLE
                itemView.setPadding(itemViewPadding, itemViewPadding, itemViewPadding, 0)
            } else {
                labelView.maxLines = 1
                expandedIndicatorView.setImageResource(R.drawable.ic_expand_more)
                expandedContentsHolder.visibility = View.GONE
                itemView.setPadding(itemViewPadding, itemViewPadding, itemViewPadding, itemViewPadding)
            }
        }
    }

    class SwipeDismissCallback(private val adapter: MessagesAdapter) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            adapter.dismissMessage(position, true)
        }
    }
}