/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.widget.configuration

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.WatchDiffCallback
import com.boswelja.devicemanager.common.recyclerview.item.IconTwoLineViewHolder
import com.boswelja.devicemanager.watchmanager.item.Watch

class WatchPickerAdapter(private val clickCallback: (watch: Watch) -> Unit) :
    ListAdapter<Watch, IconTwoLineViewHolder>(WatchDiffCallback()) {

    private val watches: ArrayList<Watch> = ArrayList()

    override fun getItemCount(): Int = watches.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconTwoLineViewHolder {
        return IconTwoLineViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: IconTwoLineViewHolder, position: Int) {
        val watch = watches[position]
        val summary = "Tap to create widget"
        holder.bind(R.drawable.ic_watch, watch.name, summary)
        holder.itemView.setOnClickListener {
            clickCallback(watch)
        }
    }
}
