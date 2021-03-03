/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.ui.OneLineViewHolder

class StringAdapter(private val strings: Array<String>) :
    RecyclerView.Adapter<OneLineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneLineViewHolder {
        return OneLineViewHolder.from(parent)
    }

    override fun getItemCount(): Int = strings.count()

    override fun onBindViewHolder(holder: OneLineViewHolder, position: Int) {
        holder.bind(strings[position])
    }
}
