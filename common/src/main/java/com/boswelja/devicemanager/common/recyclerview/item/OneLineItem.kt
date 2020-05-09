/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.recyclerview.item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.databinding.CommonRecyclerviewItemTextBinding

class OneLineItem(binding: CommonRecyclerviewItemTextBinding) :
        RecyclerView.ViewHolder(binding.root) {

    val textView: AppCompatTextView = binding.text

    companion object {
        fun create(layoutInflater: LayoutInflater, parent: ViewGroup): OneLineItem {
            val binding = CommonRecyclerviewItemTextBinding
                    .inflate(layoutInflater, parent, false)
            return OneLineItem(binding)
        }
    }
}
