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
import com.boswelja.devicemanager.common.recyclerview.item.IconOneLineItem

class MainAdapter(
        private val itemCallback: ItemClickCallback<MainItem>,
        items: ArrayList<Pair<String, ArrayList<MainItem>>>) :
        SectionedAdapter<MainItem>(items, showSectionDividers = false) {

    override fun onCreateItemViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup):
            RecyclerView.ViewHolder = IconOneLineItem.create(layoutInflater, parent)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, item: MainItem) {
        if (holder is IconOneLineItem) {
            holder.apply {
                item.enabled.also {
                    itemView.isEnabled = it
                    iconView.isEnabled = it
                    textView.isEnabled = it
                }
                iconView.setImageResource(item.iconRes)
                if (item.extra > -1) {
                    textView.text =
                            textView.context.getString(item.textRes, item.extra.toString())
                    iconView.setImageLevel(item.extra)
                } else {
                    textView.setText(item.textRes)
                    iconView.setImageLevel(0)
                }
                if (item.enabled) {
                    itemView.setOnClickListener {
                        itemCallback.onClick(item)
                    }
                }
            }
        }
    }
}
