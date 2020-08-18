/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.extensions.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.recyclerview.item.SectionHeaderViewHolder
import com.boswelja.devicemanager.ui.extensions.ExtensionItems
import com.boswelja.devicemanager.ui.extensions.Item

class ExtensionsAdapter(private val clickCallback: (item: Item) -> Unit) :
    ListAdapter<Item, RecyclerView.ViewHolder>(ItemDiffCallback()) {

  override fun getItemViewType(position: Int): Int {
    return when (val item = getItem(position)
    ) {
      is Item.Header -> item.id
      else -> super.getItemViewType(position)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      ExtensionItems.HEADER -> SectionHeaderViewHolder.from(parent)
      else -> ExtensionViewHolder.from(parent)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val context = holder.itemView.context
    val item = getItem(position)
    when (holder) {
      is ExtensionViewHolder -> {
        item as Item.Extension
        holder.bind(item)
        holder.itemView.setOnClickListener { clickCallback(item) }
      }
      is SectionHeaderViewHolder -> {
        val label = context.getString(item.textRes)
        holder.bind(label, false)
      }
    }
  }
}
