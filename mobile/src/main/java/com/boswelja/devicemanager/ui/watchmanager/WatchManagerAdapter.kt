/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.watchmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.common.WatchViewHolder
import com.boswelja.devicemanager.watchconnectionmanager.Watch

class WatchManagerAdapter(private val activity: WatchManagerActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val watches = ArrayList<Watch>()

    override fun getItemCount(): Int {
        return watches.count() + 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_ADD_WATCH
            1 -> VIEW_TYPE_SECTION_HEADER
            else -> VIEW_TYPE_WATCH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ADD_WATCH -> AddWatchViewHolder(inflater.inflate(R.layout.common_recyclerview_item_icon_one_line, parent, false))
            VIEW_TYPE_SECTION_HEADER -> SectionHeaderViewHolder(inflater.inflate(R.layout.common_recyclerview_section_header, parent, false))
            else -> WatchViewHolder(inflater.inflate(R.layout.common_recyclerview_item_icon_two_line, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context
        when (getItemViewType(position)) {
            VIEW_TYPE_ADD_WATCH -> {
                val addWatchHolder = holder as AddWatchViewHolder
                addWatchHolder.icon.setImageResource(R.drawable.ic_add)
                addWatchHolder.text.text = context.getString(R.string.watch_manager_add_watch_title)
                addWatchHolder.itemView.setOnClickListener {
                    activity.startWatchSetupActivity()
                }
            }
            VIEW_TYPE_SECTION_HEADER -> {
                val sectionHeaderHolder = holder as SectionHeaderViewHolder
                sectionHeaderHolder.label.text = context.getString(R.string.watch_manager_registered_watch_header)
            }
            VIEW_TYPE_WATCH -> {
                val watchHolder = holder as WatchViewHolder
                val watch = getWatch(position)
                watchHolder.icon.setImageResource(R.drawable.ic_watch)
                watchHolder.topLine.text = watch.name
                watchHolder.bottomLine.text = if (watch.connected) {
                    if (watch.hasApp) {
                        context.getString(R.string.watch_status_connected)
                    } else {
                        context.getString(R.string.watch_status_missing_app)
                    }
                } else {
                    context.getString(R.string.watch_status_disconnected)
                }
                watchHolder.itemView.setOnClickListener {
                    activity.startWatchInfoActivity(watch)
                }
            }
        }
    }

    private fun getWatch(position: Int) = watches[position - 2]

    fun setWatches(newWatches: List<Watch>) {
        watches.clear()
        watches.addAll(newWatches)
        notifyDataSetChanged()
    }

    fun addWatch(newWatch: Watch) {
        watches.add(newWatch)
        notifyDataSetChanged()
    }

    companion object {
        private const val VIEW_TYPE_ADD_WATCH = -1
        private const val VIEW_TYPE_SECTION_HEADER = 0
        private const val VIEW_TYPE_WATCH = 1
    }

    class AddWatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: AppCompatImageView = itemView.findViewById(R.id.icon)
        val text: AppCompatTextView = itemView.findViewById(R.id.text)
    }

    class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label: AppCompatTextView = itemView.findViewById(R.id.section_header_text)
    }
}
