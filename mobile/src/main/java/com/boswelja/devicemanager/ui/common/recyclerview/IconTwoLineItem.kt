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
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.CommonRecyclerviewItemIconTwoLineBinding

class IconTwoLineItem(binding: CommonRecyclerviewItemIconTwoLineBinding) :
        RecyclerView.ViewHolder(binding.root) {
    val iconView: AppCompatImageView = binding.icon
    val topTextView: AppCompatTextView = binding.topLine
    val bottomTextView: AppCompatTextView = binding.bottomLine

    companion object {
        fun create(layoutInflater: LayoutInflater, parent: ViewGroup): IconTwoLineItem {
            val binding =
                    DataBindingUtil.inflate<CommonRecyclerviewItemIconTwoLineBinding>(
                            layoutInflater,
                            R.layout.common_recyclerview_item_icon_two_line,
                            parent,
                            false)
            return IconTwoLineItem(binding)
        }
    }
}
