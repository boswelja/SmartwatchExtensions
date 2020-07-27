/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
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
