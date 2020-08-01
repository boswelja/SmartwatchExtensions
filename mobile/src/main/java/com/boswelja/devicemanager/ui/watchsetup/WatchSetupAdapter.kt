/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.watchsetup

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.recyclerview.item.IconTwoLineViewHolder
import com.boswelja.devicemanager.ui.common.WatchDiffCallback
import com.boswelja.devicemanager.watchmanager.WatchStatus
import com.boswelja.devicemanager.watchmanager.item.Watch

class WatchSetupAdapter(private val watchSetupFragment: WatchSetupFragment) :
    ListAdapter<Watch, IconTwoLineViewHolder>(WatchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconTwoLineViewHolder {
        return IconTwoLineViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: IconTwoLineViewHolder, position: Int) {
        val watch = getItem(position)
        val context = holder.itemView.context
        val summary = context.getString(
            when (watch.status) {
                WatchStatus.NOT_REGISTERED -> R.string.watch_status_not_registered
                WatchStatus.MISSING_APP -> R.string.watch_status_missing_app
                else -> R.string.watch_status_error
            }
        )
        holder.apply {
            bind(R.drawable.ic_watch, watch.name, summary)
            itemView.setOnClickListener {
                watchSetupFragment.confirmRegisterWatch(watch)
            }
        }
    }
}
