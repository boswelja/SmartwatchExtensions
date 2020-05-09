/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.recyclerview.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.databinding.CommonRecyclerviewSectionHeaderBinding

class SectionHeaderItem(binding: CommonRecyclerviewSectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

    val textView: AppCompatTextView = binding.sectionHeaderText
    val dividerView: View = binding.divider

    companion object {
        fun create(layoutInflater: LayoutInflater, parent: ViewGroup, showDivider: Boolean = true): SectionHeaderItem {
            val binding = CommonRecyclerviewSectionHeaderBinding
                    .inflate(layoutInflater, parent, false)
            binding.divider.visibility = if (showDivider) View.VISIBLE else View.GONE
            return SectionHeaderItem(binding)
        }
    }
}
