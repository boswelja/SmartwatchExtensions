package com.boswelja.devicemanager.ui.appmanager.adapter

import androidx.recyclerview.widget.DiffUtil

class AppDiffCallback : DiffUtil.ItemCallback<Item>() {

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }
}