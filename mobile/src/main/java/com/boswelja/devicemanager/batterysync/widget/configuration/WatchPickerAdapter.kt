/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.widget.configuration

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.recyclerview.item.IconTwoLineItem
import com.boswelja.devicemanager.watchmanager.Watch

class WatchPickerAdapter(private val activity: WatchBatteryWidgetConfigurationActivity) :
        RecyclerView.Adapter<IconTwoLineItem>() {

    private val watches: ArrayList<Watch> = ArrayList()

    private var layoutInflater: LayoutInflater? = null

    override fun getItemCount(): Int = watches.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconTwoLineItem {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.context)
        return IconTwoLineItem.create(layoutInflater!!, parent)
    }

    override fun onBindViewHolder(holder: IconTwoLineItem, position: Int) {
        val watch = watches[position]
        holder.iconView.setImageResource(R.drawable.ic_watch)
        holder.topTextView.text = watch.name
        holder.bottomTextView.text = if (watch.boolPrefs[BATTERY_SYNC_ENABLED_KEY] == true) {
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
