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
import com.boswelja.devicemanager.databinding.ExtensionSmallBinding
import com.boswelja.devicemanager.extensions.ui.Extension

class SmallExtensionViewHolder(binding: ExtensionSmallBinding) :
    ExtensionViewHolder(binding.root) {

  private val button = binding.button

  override fun bind(item: Extension) {
    button.setText(item.textRes)
    button.setIconResource(item.iconRes)
  }

  override fun setOnClickListener(clickCallback: () -> Unit) {
    button.setOnClickListener { clickCallback() }
  }

  companion object {
    fun from(parent: ViewGroup): SmallExtensionViewHolder {
      val layoutInflater = LayoutInflater.from(parent.context)
      val binding = ExtensionSmallBinding.inflate(layoutInflater, parent, false)
      return SmallExtensionViewHolder(binding)
    }
  }
}
