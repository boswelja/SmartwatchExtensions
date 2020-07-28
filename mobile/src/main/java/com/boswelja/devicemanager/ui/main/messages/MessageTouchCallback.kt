package com.boswelja.devicemanager.ui.main.messages

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.messages.Message
import kotlin.math.min

/**
 * A custom [ItemTouchHelper] that provides swipe to dismiss logic for a [Message].
 */
class MessageTouchCallback(private val adapter: MessagesAdapter, context: Context) :
        ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

    private val alphaMultiplier: Int = 3

    private val icon: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_delete)!!
    private val swipeBackground: Drawable =
            ContextCompat.getDrawable(context, R.drawable.recyclerview_swipe_background)!!
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