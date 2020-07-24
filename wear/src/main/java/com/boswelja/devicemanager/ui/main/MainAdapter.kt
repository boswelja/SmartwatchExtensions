/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.recyclerview.adapter.ItemClickCallback
import com.boswelja.devicemanager.common.recyclerview.adapter.SectionedAdapter
import com.boswelja.devicemanager.common.recyclerview.item.IconOneLineViewHolder

class MainAdapter(
    private val itemCallback: ItemClickCallback<MainItem>,
    items: ArrayList<Pair<String, ArrayList<MainItem>>>
) :
    SectionedAdapter<MainItem>(items, showSectionDividers = false) {

    override fun onCreateItemViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup):
        RecyclerView.ViewHolder = IconOneLineViewHolder.from(parent)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, item: MainItem) {
        val context = holder.itemView.context
        if (holder is IconOneLineViewHolder) {
            val text = if (item.extra > -1) {
                context.getString(item.textRes, item.extra.toString())
            } else {
                context.getString(item.textRes)
            }
            val iconLevel = if (item.extra > -1) item.extra else 0

            holder.apply {
                bind(item.iconRes, text, iconLevel)
                itemView.setOnClickListener {
                    itemCallback.onClick(item)
                }
            }
        }
    }
}
