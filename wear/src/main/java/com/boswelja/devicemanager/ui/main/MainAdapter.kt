/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.recyclerview.item.IconOneLineItem
import com.boswelja.devicemanager.common.recyclerview.item.SectionHeaderItem

class MainAdapter(private val itemCallback: ItemCallback, private vararg val sections: ArrayList<MainItem>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var layoutInflater: LayoutInflater? = null

    override fun getItemCount(): Int {
        var total = 0
        sections.forEach {
            total += it.count()
        }
        total += sections.count() - 1
        return total
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            null -> {
                ITEM_TYPE_SECTION_SEPARATOR
            }
            else -> {
                ITEM_TYPE_DEFAULT
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_TYPE_SECTION_SEPARATOR ->
                SectionHeaderItem.create(layoutInflater!!, parent, showDivider = false)
            else -> IconOneLineItem.create(layoutInflater!!, parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is IconOneLineItem -> {
                val item = getItem(position)
                if (item != null) {
                    holder.apply {
                        iconView.setImageResource(item.iconRes)
                        if (item.extra > -1) {
                            textView.text =
                                    textView.context.getString(item.textRes, item.extra.toString())
                            iconView.setImageLevel(item.extra)
                        } else {
                            textView.setText(item.textRes)
                            iconView.setImageLevel(0)
                        }
                        itemView.setOnClickListener {
                            itemCallback.onClick(item)
                        }
                    }
                }
            }
            is SectionHeaderItem -> {
                holder.apply {
                    textView.text = textView.context.getString(R.string.section_text_app_info)
                }
            }
        }
    }

    private fun getItem(position: Int): MainItem? {
        var positionInSection = position
        var sectionIndex = 0
        for (it in sections) {
            val indicesIncludingSeparator = it.count() + 1
            if (positionInSection < indicesIncludingSeparator) {
                break
            } else {
                positionInSection -= indicesIncludingSeparator
                sectionIndex++
            }
        }
        val section = sections[sectionIndex]
        return if (positionInSection in section.indices) {
            section[positionInSection]
        } else {
            null
        }
    }

    fun updateItem(newItem: MainItem) {
        var positionInAdapter = 0
        sections.forEach { section ->
            section.forEachIndexed { index, mainItem ->
                if (mainItem.itemId == newItem.itemId) {
                    section.remove(mainItem)
                    section.add(index, newItem)
                    notifyItemChanged(positionInAdapter)
                    return
                }
                positionInAdapter += 1
            }
            positionInAdapter += 1
        }
    }

    interface ItemCallback {
        fun onClick(item: MainItem)
    }

    companion object {
        private const val ITEM_TYPE_SECTION_SEPARATOR = 0
        private const val ITEM_TYPE_DEFAULT = 1
    }
}
