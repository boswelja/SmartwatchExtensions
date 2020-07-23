package com.boswelja.devicemanager.ui.appmanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.databinding.CommonRecyclerviewSectionHeaderBinding

class HeaderViewHolder private constructor(private val binding: CommonRecyclerviewSectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Item) {
        if (item is Item.Header) {
            binding.sectionHeaderText.text = item.label
        }
    }

    companion object {
        fun from(parent: ViewGroup): HeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = CommonRecyclerviewSectionHeaderBinding.inflate(layoutInflater, parent, false)
            return HeaderViewHolder(binding)
        }
    }
}