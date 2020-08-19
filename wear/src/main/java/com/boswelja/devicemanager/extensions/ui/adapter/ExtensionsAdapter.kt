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
import com.boswelja.devicemanager.extensions.ui.Size

class ExtensionsAdapter(private val clickCallback: (item: Extension) -> Unit) :
    ListAdapter<Extension, ExtensionViewHolder>(ExtensionDiffCallback()) {

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position).size) {
      Size.NORMAL -> TYPE_NORMAL
      Size.SMALL -> TYPE_SMALL
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtensionViewHolder {
    return when (viewType) {
      TYPE_NORMAL -> NormalExtensionViewHolder.from(parent)
      TYPE_SMALL -> SmallExtensionViewHolder.from(parent)
      else -> throw Exception("Unknown view type")
    }
  }

  override fun onBindViewHolder(holder: ExtensionViewHolder, position: Int) {
    val item = getItem(position)
    holder.bind(item)
    holder.setOnClickListener { clickCallback(item) }
  }

  companion object {
    private const val TYPE_NORMAL = 0
    private const val TYPE_SMALL = 1
  }
}
