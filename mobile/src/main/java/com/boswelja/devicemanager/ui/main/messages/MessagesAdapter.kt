/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
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
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.databinding.MessageItemBinding
import com.boswelja.devicemanager.messages.Action
import com.boswelja.devicemanager.messages.Message
import timber.log.Timber
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
        return MessageItemViewHolder(
            MessageItemBinding.inflate(inflater!!, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MessageItemViewHolder, position: Int) {
        val message = messages[position]

        Timber.i("Binding message")
        holder.bind(message)

        if (message.hasAction) {
            Timber.i("Setting up message action")
            holder.binding.messageActionButton.apply {
                setOnClickListener {
                    handleMessageActionClick(holder.itemView.context, message)
                }
            }
        }
    }

    /**
     * Perform the corresponding [Action] on [Message] action clicked.
     * @param context [Context].
     * @param message The [Message] whose action was clicked.
     */
    @SuppressLint("BatteryLife")
    private fun handleMessageActionClick(context: Context, message: Message) {
        Timber.d("handleMessageActionClick() called")
        when (message.action) {
            Action.DISABLE_BATTERY_OPTIMISATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }.also {
                        Timber.i("Requesting ignore battery optimisation")
                        context.startActivity(it)
                    }
                }
            }
            Action.LAUNCH_NOTIFICATION_SETTINGS -> {
                Intent().apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    } else {
                        action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        putExtra("app_package", context.packageName)
                        putExtra("app_uid", context.applicationInfo.uid)
                    }
                }.also {
                    Timber.i("Launching notification settings")
                    context.startActivity(it)
                }
            }
        }
    }

    /**
     * Adds a new [Message] to the message list and updates the UI.
     * Does nothing if the [Message] is already notified.
     * @param message The [Message] to notify.
     */
    fun notifyMessage(message: Message) {
        Timber.d("notifyMessage() called")
        if (!messages.contains(message)) {
            Timber.i("Notifying message ${message.id}")
            messages.add(message)
            messages.sortBy { it.timestamp }
            notifyItemInserted(messages.indexOf(message))
            fragment.setHasMessages(itemCount > 0)
        } else {
            Timber.w("Message ${message.id} already notified")
        }
    }

    /**
     * Removes a [Message] from the list and updates the UI.
     * @param position The position of the [Message] to dismiss.
     */
    fun dismissMessage(position: Int) {
        fragment.dismissMessage(messages[position])
        Timber.i("Dismissing message at $position")
        messages.removeAt(position)
        notifyItemRemoved(position)
        fragment.setHasMessages(itemCount > 0)
    }

    class MessageItemViewHolder(val binding: MessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.apply {
                messageIcon.setImageResource(message.iconRes)
                messageLabel.text = message.label
                messageDesc.text = message.desc
                if (message.hasAction) {
                    messageActionButton.text = message.buttonLabel
                    messageActionButton.visibility = View.VISIBLE
                    divider.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * A custom [ItemTouchHelper] that provides swipe to dismiss logic for a [Message].
     */
    class SwipeDismissCallback(private val adapter: MessagesAdapter, context: Context) :
        ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

        private val alphaMultiplier: Int = 3

        private val icon: Drawable = context.getDrawable(R.drawable.ic_delete)!!
        private val swipeBackground: Drawable = context.getDrawable(R.drawable.recyclerview_swipe_background)!!
        private val iconMargin = Utils.complexTypeDp(context.resources, 16f).toInt()
        private val iconMaxAlpha = icon.alpha

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            adapter.dismissMessage(position)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            when {
                dX > 0 -> {
                    val itemView = viewHolder.itemView
                    if (dX > icon.intrinsicWidth + iconMargin) {
                        val iconTop =
                            itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                        val iconBottom = iconTop + icon.intrinsicHeight
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = itemView.left + iconMargin + icon.intrinsicWidth
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        icon.alpha = min(
                            ((dX - icon.intrinsicWidth - iconMargin) * alphaMultiplier).toInt(),
                            iconMaxAlpha
                        )
                        icon.draw(c)
                    }

                    swipeBackground.setBounds(
                        itemView.left, itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
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
                        icon.alpha = min(
                            ((dX + icon.intrinsicWidth + iconMargin) * alphaMultiplier * -1).toInt(),
                            iconMaxAlpha
                        )
                        icon.draw(c)
                    }

                    swipeBackground.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top, itemView.right, itemView.bottom
                    )
                    swipeBackground.draw(c)
                }
            }
        }
    }
}
