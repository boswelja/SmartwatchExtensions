package com.boswelja.devicemanager.messages.ui

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R

/**
 * Callback for handling swipe to dismiss logic. This also handles showing the dismiss icon under
 * swiped items.
 */
class SwipeDismissCallback(
    context: Context,
    private val itemDismissCallback: (position: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {

    private val background = ContextCompat.getColor(context, R.color.dividerColor).toDrawable()
    private val iconMargin = context.resources.getDimension(R.dimen.message_item_padding).toInt()
    private val iconSize = context.resources.getDimension(R.dimen.swipe_icon_size).toInt()
    private val icon = ContextCompat.getDrawable(context, R.drawable.ic_delete)!!

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.absoluteAdapterPosition
        itemDismissCallback(position)
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
        val verticalIconMargin = calculateVerticalMargin(
            viewHolder.itemView.top,
            viewHolder.itemView.bottom
        )
        if (dX >= 0) {
            background.setBounds(
                0,
                viewHolder.itemView.top,
                (viewHolder.itemView.left + dX).toInt(),
                viewHolder.itemView.bottom
            )
            icon.setBounds(
                iconMargin,
                verticalIconMargin + viewHolder.itemView.top,
                iconMargin + iconSize,
                viewHolder.itemView.bottom - verticalIconMargin
            )
        } else {
            background.setBounds(
                (viewHolder.itemView.right + dX).toInt(),
                viewHolder.itemView.top,
                viewHolder.itemView.right,
                viewHolder.itemView.bottom
            )
            icon.setBounds(
                viewHolder.itemView.right - iconMargin - iconSize,
                verticalIconMargin + viewHolder.itemView.top,
                viewHolder.itemView.right - iconMargin,
                viewHolder.itemView.bottom - verticalIconMargin
            )
        }
        background.draw(c)
        icon.draw(c)
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun calculateVerticalMargin(top: Int, bottom: Int): Int {
        return (bottom - top - iconSize) / 2
    }
}
