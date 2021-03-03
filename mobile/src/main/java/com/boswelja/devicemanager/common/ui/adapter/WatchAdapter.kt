/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.WatchDiffCallback
import com.boswelja.devicemanager.common.ui.IconTwoLineViewHolder
import com.boswelja.devicemanager.watchmanager.item.Watch

open class WatchAdapter(private val clickCallback: ((watch: Watch) -> Unit)?) :
    ListAdapter<Watch, RecyclerView.ViewHolder>(WatchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return IconTwoLineViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as IconTwoLineViewHolder
        val watch = getItem(position)
        val context = holder.itemView.context
        val summary =
            context.getString(
                when (watch.status) {
                    Watch.Status.NOT_REGISTERED -> R.string.watch_status_not_registered
                    Watch.Status.MISSING_APP -> R.string.watch_status_missing_app
                    Watch.Status.UNKNOWN -> R.string.watch_status_unknown
                    Watch.Status.ERROR -> R.string.watch_status_error
                    Watch.Status.DISCONNECTED -> R.string.watch_status_disconnected
                    Watch.Status.CONNECTED -> R.string.watch_status_connected
                }
            )
        holder.apply {
            bind(R.drawable.ic_watch, watch.name, summary)
            if (clickCallback != null) {
                itemView.setOnClickListener { clickCallback.invoke(watch) }
            }
        }
    }
}
