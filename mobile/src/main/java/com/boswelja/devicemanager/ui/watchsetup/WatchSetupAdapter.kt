/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.watchsetup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.common.recyclerview.IconTwoLineItem
import com.boswelja.devicemanager.watchmanager.Watch
import com.boswelja.devicemanager.watchmanager.WatchStatus

class WatchSetupAdapter(private val watchSetupFragment: WatchSetupFragment) :
        RecyclerView.Adapter<IconTwoLineItem>() {

    private val watches = ArrayList<Watch>()

    private var layoutInflater: LayoutInflater? = null

    override fun getItemCount(): Int = watches.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconTwoLineItem {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.context)
        return IconTwoLineItem.create(layoutInflater!!, parent)
    }

    override fun onBindViewHolder(holder: IconTwoLineItem, position: Int) {
        val watch = watches[position]
        val context = holder.itemView.context
        holder.apply {
            iconView.setImageResource(R.drawable.ic_watch)
            topTextView.text = watch.name
            bottomTextView.text = context.getString(when (watch.status) {
                WatchStatus.NOT_REGISTERED -> R.string.watch_status_not_registered
                WatchStatus.MISSING_APP -> R.string.watch_status_missing_app
                else -> R.string.watch_status_error
            })
            itemView.setOnClickListener {
                watchSetupFragment.confirmRegisterWatch(watch)
            }
        }
    }

    /**
     * Set the [List] of [Watch] objects to show.
     * @param newWatches The new [Watch] objects to show.
     */
    fun setWatches(newWatches: List<Watch>) {
        watches.apply {
            clear()
            addAll(newWatches)
        }
        notifyDataSetChanged()
    }
}
