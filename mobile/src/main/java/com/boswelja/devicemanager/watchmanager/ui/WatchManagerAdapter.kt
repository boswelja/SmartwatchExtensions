package com.boswelja.devicemanager.watchmanager.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.IconOneLineViewHolder
import com.boswelja.devicemanager.common.ui.SectionHeaderViewHolder
import com.boswelja.devicemanager.common.ui.adapter.WatchAdapter
import com.boswelja.devicemanager.watchmanager.item.Watch

class WatchManagerAdapter(private val clickListener: (watch: Watch?) -> Unit) :
    WatchAdapter(clickListener) {

    override fun getItemCount(): Int = super.getItemCount() + 2

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_ADD_WATCH
            1 -> VIEW_TYPE_SECTION_HEADER
            else -> VIEW_TYPE_WATCH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD_WATCH -> IconOneLineViewHolder.from(parent)
            VIEW_TYPE_SECTION_HEADER -> SectionHeaderViewHolder.from(parent)
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context
        when (holder) {
            is IconOneLineViewHolder -> {
                holder.bind(
                    R.drawable.ic_add, context.getString(R.string.watch_manager_add_watch_title)
                )
                holder.itemView.setOnClickListener { clickListener(null) }
            }
            is SectionHeaderViewHolder -> {
                holder.bind(context.getString(R.string.watch_manager_registered_watch_header))
            }
            else -> super.onBindViewHolder(holder, position - 2)
        }
    }

    companion object {
        private const val VIEW_TYPE_ADD_WATCH = -1
        private const val VIEW_TYPE_SECTION_HEADER = 0
        private const val VIEW_TYPE_WATCH = 1
    }
}
