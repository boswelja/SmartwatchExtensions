/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.common.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.databinding.CommonRecyclerviewItemSeparatorBinding

class ItemSeparator(binding: CommonRecyclerviewItemSeparatorBinding) :
        RecyclerView.ViewHolder(binding.root) {

    val dividerView: View = binding.divider

    companion object {
        fun create(layoutInflater: LayoutInflater, parent: ViewGroup): ItemSeparator {
            return ItemSeparator(CommonRecyclerviewItemSeparatorBinding
                    .inflate(layoutInflater, parent, false))
        }
    }
}
