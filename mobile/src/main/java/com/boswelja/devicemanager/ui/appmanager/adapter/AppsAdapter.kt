/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.recyclerview.adapter.ItemClickCallback

class AppsAdapter(private val itemClickCallback: ItemClickCallback<Item>) :
    ListAdapter<Item, RecyclerView.ViewHolder>(AppDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Item.Header -> TYPE_HEADER
            else -> TYPE_APP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder.from(parent)
            else -> AppViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(item)
            is AppViewHolder -> holder.bind(item)
        }
        holder.itemView.setOnClickListener { itemClickCallback.onClick(item) }
    }

    companion object {
        private const val TYPE_APP = 0
        private const val TYPE_HEADER = 1
    }
}
