package com.boswelja.devicemanager.ui.common

import androidx.recyclerview.widget.DiffUtil
import com.boswelja.devicemanager.watchmanager.item.Watch

class WatchDiffCallback : DiffUtil.ItemCallback<Watch>() {

    override fun areContentsTheSame(oldItem: Watch, newItem: Watch): Boolean {
        return oldItem.id == newItem.id &&
                oldItem.name == newItem.id
    }

    override fun areItemsTheSame(oldItem: Watch, newItem: Watch): Boolean {
        return oldItem == newItem
    }
}