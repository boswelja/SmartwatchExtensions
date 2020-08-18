/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.extensions.adapter

import androidx.recyclerview.widget.DiffUtil
import com.boswelja.devicemanager.ui.extensions.Item

class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {

  override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
    return oldItem.hashCode() == newItem.hashCode()
  }

  override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
    return oldItem.id == newItem.id
  }
}
