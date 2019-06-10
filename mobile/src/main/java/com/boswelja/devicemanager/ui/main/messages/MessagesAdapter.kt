/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.messages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.Drawable
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

    class SwipeDismissCallback(private val adapter: MessagesAdapter, context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        private val alphaMultiplier: Int = 3

        private val icon: Drawable = context.getDrawable(R.drawable.ic_delete)!!
        private val swipeToLeftBackground: Drawable = context.getDrawable(R.drawable.recyclerview_swipe_to_left_background)!!
        private val swipeToRightBackground = context.getDrawable(R.drawable.recyclerview_swipe_to_right_background)!!
        private val backgroundCornerOffset = context.resources.getDimension(R.dimen.corner_radius).toInt() * 2
        private val iconMargin = Utils.complexTypeDp(context.resources, 8f).toInt()
        private val iconMaxAlpha = icon.alpha

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            adapter.dismissMessage(position, true)
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            when {
                dX > 0 -> {
                    val itemView = viewHolder.itemView
                    if (dX > icon.intrinsicWidth + iconMargin) {
                        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                        val iconBottom = iconTop + icon.intrinsicHeight
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = itemView.left + iconMargin + icon.intrinsicWidth
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        icon.alpha = Math.min(((dX - icon.intrinsicWidth - iconMargin) * alphaMultiplier).toInt(), iconMaxAlpha)
                        icon.draw(c)
                    }

                    swipeToRightBackground.setBounds(itemView.left, itemView.top,
                            itemView.left + dX.toInt() + backgroundCornerOffset,
                            itemView.bottom)
                    swipeToRightBackground.draw(c)
                }
                dX < 0 -> {
                    val itemView = viewHolder.itemView
                    if (dX < 0 - icon.intrinsicWidth - iconMargin) {
                        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                        val iconBottom = iconTop + icon.intrinsicHeight
                        val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        icon.alpha = Math.min(((dX + icon.intrinsicWidth + iconMargin) * alphaMultiplier * -1).toInt(), iconMaxAlpha)
                        icon.draw(c)
                    }

                    swipeToLeftBackground.setBounds(itemView.right + dX.toInt() - backgroundCornerOffset,
                            itemView.top, itemView.right, itemView.bottom)
                    swipeToLeftBackground.draw(c)
                }
            }
        }
    }
}
