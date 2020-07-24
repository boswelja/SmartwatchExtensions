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
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.databinding.RecyclerviewItemOneLineBinding

class OneLineViewHolder private constructor(private val binding: RecyclerviewItemOneLineBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(text: String) {
        binding.text.text = text
    }

    companion object {
        fun from(parent: ViewGroup): OneLineViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecyclerviewItemOneLineBinding
                    .inflate(layoutInflater, parent, false)
            return OneLineViewHolder(binding)
        }
    }
}
