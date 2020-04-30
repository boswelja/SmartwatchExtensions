/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.common.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.databinding.CommonRecyclerviewItemIconOneLineSmallBinding

class SmallItemIconOneLine(binding: CommonRecyclerviewItemIconOneLineSmallBinding) :
        RecyclerView.ViewHolder(binding.root) {

    val iconView: AppCompatImageView = binding.iconView
    val textView: AppCompatTextView = binding.textView

    companion object {
        fun create(layoutInflater: LayoutInflater, parent: ViewGroup): SmallItemIconOneLine {
            return SmallItemIconOneLine(CommonRecyclerviewItemIconOneLineSmallBinding
                    .inflate(layoutInflater, parent, false))
        }
    }
}
