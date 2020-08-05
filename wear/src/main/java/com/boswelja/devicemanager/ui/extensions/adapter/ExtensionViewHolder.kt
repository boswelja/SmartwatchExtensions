/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.extensions.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.boswelja.devicemanager.common.databinding.RecyclerviewItemIconOneLineBinding
import com.boswelja.devicemanager.common.recyclerview.item.IconOneLineViewHolder
import com.boswelja.devicemanager.ui.extensions.Item
import timber.log.Timber

class ExtensionViewHolder(private val binding: RecyclerviewItemIconOneLineBinding) :
    IconOneLineViewHolder(binding) {

    fun bind(item: Item.Extension) {
        val context = itemView.context
        val lifecycleOwner = context as LifecycleOwner
        item.isEnabled.observe(lifecycleOwner) {
            Timber.i("Setting ${item.id} enabled to $it")
            itemView.isEnabled = it
            if (!it) binding.text.text = context.getString(item.disabledTextRes)
            else if (item.extra.value!! >= 0) binding.text.text = context.getString(item.textRes, item.extra.value)
            else binding.text.text = context.getString(item.textRes)
        }
        item.extra.observe(lifecycleOwner) {
            if (item.isEnabled.value == true && it >= 0) {
                Timber.i("Setting ${item.id} extra to $it")
                binding.text.text = context.getString(item.textRes, it)
                binding.icon.setImageLevel(it)
            }
        }
        binding.icon.setImageResource(item.iconRes)
    }

    companion object {
        fun from(parent: ViewGroup): ExtensionViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecyclerviewItemIconOneLineBinding
                .inflate(layoutInflater, parent, false)
            return ExtensionViewHolder(binding)
        }
    }
}
