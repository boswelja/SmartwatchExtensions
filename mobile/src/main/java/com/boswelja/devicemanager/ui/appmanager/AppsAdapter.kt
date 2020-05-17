/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.common.recyclerview.adapter.ItemClickCallback
import com.boswelja.devicemanager.common.recyclerview.adapter.SectionedAdapter
import com.boswelja.devicemanager.common.recyclerview.item.IconTwoLineItem

class AppsAdapter(private val itemClickCallback: ItemClickCallback<AppPackageInfo>) :
        SectionedAdapter<AppPackageInfo>() {

    private var fallbackIcon: Drawable? = null

    override fun onCreateItemViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup):
            RecyclerView.ViewHolder = IconTwoLineItem.create(layoutInflater, parent)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, item: AppPackageInfo) {
        val context = holder.itemView.context
        if (fallbackIcon == null) fallbackIcon = context.getDrawable(R.drawable.ic_app_icon_unknown)
        if (holder is IconTwoLineItem) {
            holder.apply {
                topTextView.text = item.packageLabel
                bottomTextView.text = item.versionName
                iconView.setImageDrawable(Utils.getAppIcon(context, item.packageName, fallbackIcon))
                itemView.setOnClickListener {
                    itemClickCallback.onClick(item)
                }
            }
        }
    }

    fun removeByPackageName(packageName: String): AppPackageInfo? {
        items.forEach {
            val item = it.second.firstOrNull { item -> item.packageName == packageName }
            if (item?.packageName == packageName) {
                it.second.remove(item)
                return item
            }
        }
        return null
    }
}
