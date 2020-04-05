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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.databinding.FragmentMessagesItemBinding
import com.boswelja.devicemanager.messages.Message
import com.boswelja.devicemanager.messages.MessageId
import kotlin.math.min

internal class MessagesAdapter(private val fragment: MessageFragment) :
        RecyclerView.Adapter<MessagesAdapter.MessageItemViewHolder>() {

    private val messages = ArrayList<Message>()

    private var inflater: LayoutInflater? = null

    override fun getItemCount(): Int = messages.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageItemViewHolder {
        if (inflater == null) {
            inflater = LayoutInflater.from(parent.context)
        }
        return MessageItemViewHolder(DataBindingUtil.inflate(
                inflater!!,
                R.layout.fragment_messages_item,
                parent,
                false))
    }

    override fun onBindViewHolder(holder: MessageItemViewHolder, position: Int) {
        val message = messages[position]

        holder.bind(message)

        if (holder.binding.actionVisible) {
            holder.binding.messageActionButton.apply {
                setOnClickListener {
                    handleMessageActionClick(holder, message)
                }
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun handleMessageActionClick(holder: MessageItemViewHolder, message: Message) {
        val context = holder.itemView.context
        when (message.id) {
            MessageId.BATTERY_OPT_ENABLED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context?.packageName}")
                    }.also {
                        context.startActivity(it)
                    }
                }
            }
            MessageId.BATTERY_NOTIS_DISABLED -> {
                Intent().apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName!!)
                    } else {
                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        putExtra("app_package", context?.packageName!!)
                        putExtra("app_uid", context.applicationInfo?.uid!!)
                    }
                }.also { context.startActivity(it) }
            }
        }
    }

    fun notifyMessage(message: Message) {
        if (!messages.contains(message)) {
            messages.add(message)
            notifyItemInserted(messages.indexOf(message))
            fragment.setHasMessages(itemCount > 0)
        }
    }

    internal fun dismissMessage(position: Int) {
        fragment.dismissMessage(messages[position])
        messages.removeAt(position)
        notifyItemRemoved(position)
        fragment.setHasMessages(itemCount > 0)
    }

    class MessageItemViewHolder(val binding: FragmentMessagesItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.actionVisible = message.buttonLabelRes > 0
            binding.message = message
        }
    }

    class SwipeDismissCallback(private val adapter: MessagesAdapter, context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        private val alphaMultiplier: Int = 3

        private val icon: Drawable = context.getDrawable(R.drawable.ic_delete)!!
        private val swipeBackground: Drawable = context.getDrawable(R.drawable.recyclerview_swipe_background)!!
        private val iconMargin = Utils.complexTypeDp(context.resources, 16f).toInt()
        private val iconMaxAlpha = icon.alpha

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            adapter.dismissMessage(position)
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
                        icon.alpha = min(((dX - icon.intrinsicWidth - iconMargin) * alphaMultiplier).toInt(), iconMaxAlpha)
                        icon.draw(c)
                    }

                    swipeBackground.setBounds(itemView.left, itemView.top,
                            itemView.left + dX.toInt(),
                            itemView.bottom)
                    swipeBackground.draw(c)
                }
                dX < 0 -> {
                    val itemView = viewHolder.itemView
                    if (dX < 0 - icon.intrinsicWidth - iconMargin) {
                        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                        val iconBottom = iconTop + icon.intrinsicHeight
                        val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        icon.alpha = min(((dX + icon.intrinsicWidth + iconMargin) * alphaMultiplier * -1).toInt(), iconMaxAlpha)
                        icon.draw(c)
                    }

                    swipeBackground.setBounds(itemView.right + dX.toInt(),
                            itemView.top, itemView.right, itemView.bottom)
                    swipeBackground.draw(c)
                }
            }
        }
    }
}
