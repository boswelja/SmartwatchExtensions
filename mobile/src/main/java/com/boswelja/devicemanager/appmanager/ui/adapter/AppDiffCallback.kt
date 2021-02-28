package com.boswelja.devicemanager.appmanager.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.boswelja.devicemanager.common.appmanager.App

class AppDiffCallback : DiffUtil.ItemCallback<App>() {

    override fun areContentsTheSame(oldItem: App, newItem: App): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: App, newItem: App): Boolean {
        return oldItem.packageName == newItem.packageName
    }
}
