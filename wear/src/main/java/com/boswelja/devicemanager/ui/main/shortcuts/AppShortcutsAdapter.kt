/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.shortcuts

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.databinding.CommonItemSmallBinding
import com.boswelja.devicemanager.ui.settings.SettingsActivity

class AppShortcutsAdapter : RecyclerView.Adapter<AppShortcutsAdapter.ViewHolder>() {

    private var layoutInflater: LayoutInflater? = null

    private val items = AppShortcut.values()

    override fun getItemCount(): Int = items.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }
        val binding = CommonItemSmallBinding.inflate(layoutInflater!!, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shortcut = items[position]
        holder.binding.apply {
            iconView.setImageResource(shortcut.iconRes)
            labelView.setText(shortcut.labelRes)
        }
        holder.itemView.setOnClickListener {
            when (shortcut) {
                AppShortcut.Settings -> {
                    Intent(it.context, SettingsActivity::class.java).also { intent ->
                        it.context.startActivity(intent)
                    }
                }
            }
        }
    }

    inner class ViewHolder(val binding: CommonItemSmallBinding) : RecyclerView.ViewHolder(binding.root)
}
