/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.databinding.AppManagerItemBinding

class AppViewHolder private constructor(private val binding: AppManagerItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Item) {
        if (item is Item.App) {
            binding.appInfo = item
            binding.executePendingBindings()
        }
    }

    companion object {
        fun from(parent: ViewGroup): AppViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = AppManagerItemBinding.inflate(layoutInflater, parent, false)
            return AppViewHolder(binding)
        }
    }
}
