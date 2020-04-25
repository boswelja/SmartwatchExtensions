/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
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
import com.boswelja.devicemanager.watchmanager.Watch
import com.boswelja.devicemanager.watchmanager.WatchStatus

class WatchManagerAdapter(private val activity: WatchManagerActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val watches = ArrayList<Watch>()

    override fun getItemCount(): Int = watches.count() + 2

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
            VIEW_TYPE_ADD_WATCH -> AddWatchViewHolder(inflater.inflate(
                    R.layout.common_recyclerview_item_icon_one_line,
                    parent, false))
            VIEW_TYPE_SECTION_HEADER -> SectionHeaderViewHolder(inflater.inflate(
                    R.layout.common_recyclerview_section_header,
                    parent, false))
            else -> WatchViewHolder(inflater.inflate(
                    R.layout.common_recyclerview_item_icon_two_line,
                    parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context
        when (holder) {
            is AddWatchViewHolder -> {
                holder.apply {
                    icon.setImageResource(R.drawable.ic_add)
                    text.text = context.getString(R.string.watch_manager_add_watch_title)
                    itemView.setOnClickListener {
                        activity.openWatchSetupActivity()
                    }
                }
            }
            is SectionHeaderViewHolder -> {
                holder.label.text = context.getString(R.string.watch_manager_registered_watch_header)
            }
            is WatchViewHolder -> {
                val watch = getWatch(position)
                holder.apply {
                    icon.setImageResource(R.drawable.ic_watch)
                    topLine.text = watch.name
                    bottomLine.text = context.getString(when (watch.status) {
                        WatchStatus.CONNECTED -> R.string.watch_status_connected
                        WatchStatus.DISCONNECTED -> R.string.watch_status_disconnected
                        WatchStatus.MISSING_APP -> R.string.watch_status_missing_app
                        else -> R.string.watch_status_error
                    })
                    itemView.setOnClickListener {
                        activity.openWatchInfoActivity(watch)
                    }
                }
            }
        }
    }

    private fun getWatch(position: Int) = watches[position - 2]

    /**
     * Sets the [List] of [Watch] objects to show.
     * @param newWatches The new [List] of [Watch] objects to show.
     */
    fun setWatches(newWatches: List<Watch>) {
        watches.apply {
            clear()
            addAll(newWatches)
        }
        notifyDataSetChanged()
    }

    /**
     * Add a [Watch] to the adapter.
     * @param newWatch The new [Watch] to add.
     */
    fun addWatch(newWatch: Watch) {
        watches.add(newWatch)
        notifyDataSetChanged()
    }

    /**
     * Updates the given [Watch] in the list.
     * Matches [Watch.id] from the given watch.
     * @param watch The [Watch] to update.
     */
    fun updateWatch(watch: Watch) {
        val index = watches.indexOfFirst { it.id == watch.id }
        watches.removeAt(index)
        watches.add(index, watch)
        notifyItemChanged(index + 2)
    }

    /**
     * Removes a [Watch] by it's ID.
     * @param watchId The ID of the [Watch] to remove.
     */
    fun removeWatch(watchId: String) {
        val index = watches.indexOfFirst { it.id == watchId }
        watches.removeAt(index)
        notifyItemRemoved(index + 2)
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
