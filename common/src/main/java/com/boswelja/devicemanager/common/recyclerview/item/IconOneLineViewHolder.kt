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
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.databinding.RecyclerviewItemIconOneLineBinding

open class IconOneLineViewHolder(private val binding: RecyclerviewItemIconOneLineBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(@DrawableRes icon: Int, text: String, iconLevel: Int = 0) {
        binding.icon.setImageResource(icon)
        binding.icon.setImageLevel(iconLevel)
        binding.text.text = text
    }

    companion object {
        fun from(parent: ViewGroup): IconOneLineViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecyclerviewItemIconOneLineBinding.inflate(layoutInflater, parent, false)
            return IconOneLineViewHolder(binding)
        }
    }
}
