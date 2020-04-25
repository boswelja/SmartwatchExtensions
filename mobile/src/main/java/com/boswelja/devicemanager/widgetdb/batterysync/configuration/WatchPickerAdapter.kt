/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.widgetdb.batterysync.configuration

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.ui.common.WatchViewHolder
import com.boswelja.devicemanager.watchmanager.Watch

class WatchPickerAdapter(private val activity: WatchBatteryWidgetConfigurationActivity) :
        RecyclerView.Adapter<WatchViewHolder>() {

    private val watches: ArrayList<Watch> = ArrayList()

    override fun getItemCount(): Int = watches.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchViewHolder {
        return WatchViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.common_recyclerview_item_icon_two_line, parent, false))
    }

    override fun onBindViewHolder(holder: WatchViewHolder, position: Int) {
        val watch = watches[position]
        holder.icon.setImageResource(R.drawable.ic_watch)
        holder.topLine.text = watch.name
        holder.bottomLine.text = if (watch.boolPrefs[BATTERY_SYNC_ENABLED_KEY] == true) {
            "Tap to create widget"
        } else {
            "Battery Sync disabled for this watch"
        }
        holder.itemView.setOnClickListener {
            if (watch.boolPrefs[BATTERY_SYNC_ENABLED_KEY] == true) {
                activity.finishAndCreateWidget(watch.id)
            }
        }
    }

    /**
     * Sets the list of [Watch] objects to show in the adapter.
     * @param newWatches The new list of watches to show.
     */
    fun setWatches(newWatches: List<Watch>) {
        watches.apply {
            clear()
            addAll(newWatches)
        }
        notifyDataSetChanged()
    }
}
