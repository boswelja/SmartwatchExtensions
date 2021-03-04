package com.boswelja.devicemanager.common.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.databinding.RecyclerviewSectionHeaderBinding

open class SectionHeaderViewHolder(
    private val binding: RecyclerviewSectionHeaderBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(text: String) {
        binding.title.text = text
    }

    companion object {
        fun from(parent: ViewGroup): SectionHeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = RecyclerviewSectionHeaderBinding.inflate(layoutInflater, parent, false)
            return SectionHeaderViewHolder(binding)
        }
    }
}
