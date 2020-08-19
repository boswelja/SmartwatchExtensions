/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.extensions.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.boswelja.devicemanager.databinding.ExtensionNormalBinding
import com.boswelja.devicemanager.databinding.ExtensionSmallBinding
import com.boswelja.devicemanager.extensions.ui.Extension
import timber.log.Timber

class NormalExtensionViewHolder(private val binding: ExtensionNormalBinding) :
    ExtensionViewHolder(binding.root) {

  override fun bind(item: Extension) {
    val context = itemView.context
    val lifecycleOwner = context as LifecycleOwner
    item.isEnabled.observe(lifecycleOwner) {
      Timber.i("Setting ${item.id} enabled to $it")
      itemView.isEnabled = it
      if (!it) binding.text.text = context.getString(item.disabledTextRes)
      else if (item.extra.value!! >= 0)
          binding.text.text = context.getString(item.textRes, item.extra.value)
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

  override fun setOnClickListener(clickCallback: () -> Unit) {
    itemView.setOnClickListener { clickCallback() }
  }

  companion object {
    fun from(parent: ViewGroup): NormalExtensionViewHolder {
      val layoutInflater = LayoutInflater.from(parent.context)
      val binding = ExtensionNormalBinding.inflate(layoutInflater, parent, false)
      return NormalExtensionViewHolder(binding)
    }
  }
}
