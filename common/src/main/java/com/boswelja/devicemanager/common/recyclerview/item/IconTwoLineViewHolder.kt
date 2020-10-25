/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.recyclerview.item

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.databinding.RecyclerviewItemIconTwoLineBinding

open class IconTwoLineViewHolder(private val binding: RecyclerviewItemIconTwoLineBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private fun bindText(title: String, summary: String) {
        binding.topLine.text = title
        binding.bottomLine.text = summary
    }

    fun bind(@DrawableRes icon: Int, title: String, summary: String) {
        binding.icon.setImageResource(icon)
        bindText(title, summary)
    }

    fun bind(icon: Bitmap, title: String, summary: String) {
        binding.icon.setImageBitmap(icon)
        bindText(title, summary)
    }

    companion object {
        fun from(parent: ViewGroup): IconTwoLineViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecyclerviewItemIconTwoLineBinding.inflate(layoutInflater, parent, false)
            return IconTwoLineViewHolder(binding)
        }
    }
}
