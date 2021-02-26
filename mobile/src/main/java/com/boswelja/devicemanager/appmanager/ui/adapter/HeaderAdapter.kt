package com.boswelja.devicemanager.appmanager.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.recyclerview.item.SectionHeaderViewHolder

class HeaderAdapter(private val title: String) : RecyclerView.Adapter<SectionHeaderViewHolder>() {

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionHeaderViewHolder {
        return SectionHeaderViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SectionHeaderViewHolder, position: Int) {
        holder.bind(title)
    }
}
