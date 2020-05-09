/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.changelog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.recyclerview.item.OneLineItem

class ChangelogAdapter(private val changelog: Array<String>) : RecyclerView.Adapter<OneLineItem>() {

    private var layoutInflater: LayoutInflater? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneLineItem {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.context)
        return OneLineItem.create(layoutInflater!!, parent)
    }

    override fun getItemCount(): Int = changelog.count()

    override fun onBindViewHolder(holder: OneLineItem, position: Int) {
        holder.textView.text = changelog[position]
    }
}
