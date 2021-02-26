/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.recyclerview.item.IconTwoLineViewHolder

class AppsAdapter(private val clickCallback: (App) -> Unit) :
    ListAdapter<App, IconTwoLineViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconTwoLineViewHolder {
        return IconTwoLineViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: IconTwoLineViewHolder, position: Int) {
        val app = getItem(position)
        if (app.packageIcon?.bitmap != null) {
            holder.bind(
                app.packageIcon!!.bitmap,
                app.packageLabel,
                app.versionName ?: app.versionCode.toString()
            )
        } else {
            holder.bind(
                R.drawable.android_head,
                app.packageLabel,
                app.versionName ?: app.versionCode.toString()
            )
        }
        holder.itemView.setOnClickListener { clickCallback(app) }
    }
}
