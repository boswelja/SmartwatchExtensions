/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.extensions.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.boswelja.devicemanager.extensions.ui.Extension

class ExtensionsAdapter(private val clickCallback: (item: Extension) -> Unit) :
    ListAdapter<Extension, ExtensionViewHolder>(ExtensionDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtensionViewHolder {
    return  ExtensionViewHolder.from(parent)
  }

  override fun onBindViewHolder(holder: ExtensionViewHolder, position: Int) {
    val item = getItem(position)
    holder.bind(item)
    holder.itemView.setOnClickListener { clickCallback(item) }
  }
}
