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
import com.boswelja.devicemanager.ui.common.recyclerview.BigItemIconOneLine
import com.boswelja.devicemanager.ui.common.recyclerview.ItemSeparator
import com.boswelja.devicemanager.ui.common.recyclerview.SmallItemIconOneLine

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
        val item = getItem(position)
        return when {
            item == null -> {
                ITEM_TYPE_SEPARATOR
            }
            item.isSmallItem -> {
                ITEM_TYPE_SMALL_ITEM
            }
            else -> {
                ITEM_TYPE_DEFAULT
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_TYPE_SEPARATOR -> ItemSeparator.create(layoutInflater!!, parent)
            ITEM_TYPE_SMALL_ITEM -> SmallItemIconOneLine.create(layoutInflater!!, parent)
            else -> BigItemIconOneLine.create(layoutInflater!!, parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SmallItemIconOneLine -> {
                val item = getItem(position)
                if (item != null) {
                    holder.apply {
                        iconView.setImageResource(item.iconRes)
                        textView.setText(item.textRes)
                        itemView.setOnClickListener {
                            itemCallback.onClick(item)
                        }
                    }
                }
            }
            is BigItemIconOneLine -> {
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
        private const val ITEM_TYPE_SEPARATOR = 0
        private const val ITEM_TYPE_DEFAULT = 1
        private const val ITEM_TYPE_SMALL_ITEM = 2
    }
}
