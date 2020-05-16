/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.recyclerview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.recyclerview.item.SectionHeaderItem

/**
 * A custom [RecyclerView.Adapter] set up for sections.
 * @property T The item type to display.
 * @param items An [ArrayList] of [Pair] objects that correspond to sections. Each pair should
 * contain a [String] for the section label (or [SECTION_HEADER_HIDDEN] if there shouldn't be a label), and
 * an [ArrayList] of items to show in the section.
 * @param showSectionDividers Whether section headers should show a divider as well.
 */
abstract class SectionedAdapter<T>(
    private val items: ArrayList<Pair<String, ArrayList<T>>> = ArrayList(),
    private val showSectionDividers: Boolean = true
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var layoutInflater: LayoutInflater? = null

    private var sectionCount: Int = 0
    private var itemCount: Int = 0

    init {
        updateSectionCount()
        updateItemCount()
    }

    abstract fun onCreateItemViewHolder(layoutInflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder
    abstract fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, item: T)

    override fun getItemCount(): Int =
            sectionCount + itemCount

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            null -> ITEM_TYPE_SECTION_HEADER
            else -> ITEM_TYPE_DEFAULT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_TYPE_SECTION_HEADER ->
                SectionHeaderItem.create(layoutInflater!!, parent, showDivider = showSectionDividers)
            else -> onCreateItemViewHolder(layoutInflater!!, parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SectionHeaderItem -> {
                val section = getSection(position)
                holder.apply {
                    textView.text = section
                }
            }
            else -> onBindItemViewHolder(holder, getItem(position)!!)
        }
    }

    /**
     * Updates the stored [sectionCount].
     */
    private fun updateSectionCount() {
        sectionCount = items.count { it.first != SECTION_HEADER_HIDDEN }
    }

    /**
     * Updates the stored [itemCount].
     */
    private fun updateItemCount() {
        itemCount = 0
        items.forEach {
            itemCount += it.second.count()
        }
    }

    /**
     * Gets an item [T] at a given position, or null if the position is a section.
     * @param position The position of the item to get.
     * @return The item [T] at the given position, or null.
     */
    private fun getItem(position: Int): T? {
        var positionInSection = position
        var sectionIndex = 0
        for (it in items) {
            val section = it.first
            val sectionItems = it.second
            val countIncludingSection = if (section != SECTION_HEADER_HIDDEN) {
                sectionItems.count() + 1
            } else {
                sectionItems.count()
            }
            if (positionInSection < countIncludingSection) {
                break
            } else {
                positionInSection -= countIncludingSection
                sectionIndex++
            }
        }
        val section = items[sectionIndex]
        val sectionItems = section.second
        return if (section.first != SECTION_HEADER_HIDDEN) {
            if (positionInSection in 1..sectionItems.count()) {
                sectionItems[positionInSection - 1]
            } else {
                null
            }
        } else {
            if (positionInSection in sectionItems.indices) {
                sectionItems[positionInSection]
            } else {
                null
            }
        }
    }

    /**
     * Gets the section label at a given position.
     * @param position The position of the section to get.
     * @return The label of the section for the given position.
     */
    private fun getSection(position: Int): String {
        var positionInSection = position
        var sectionIndex = 0
        for (it in items) {
            val section = it.first
            val sectionItems = it.second
            val countIncludingSection = if (section != SECTION_HEADER_HIDDEN) {
                sectionItems.count() + 1
            } else {
                sectionItems.count()
            }
            if (positionInSection < countIncludingSection) {
                break
            } else {
                positionInSection -= countIncludingSection
                sectionIndex++
            }
        }
        return items[sectionIndex].first
    }

    /**
     * Replaces all items in the adapter with a given set of new items.
     * @param newItems The new items to populate the adapter with.
     */
    fun setItems(newItems: ArrayList<Pair<String, ArrayList<T>>>) {
        items.apply {
            clear()
            addAll(newItems)
        }
        updateSectionCount()
        updateItemCount()
        notifyDataSetChanged()
    }

    /**
     * Adds a new section to the data set and notifies the adapter.
     * @param section The [Pair] representing the new section.
     */
    fun addSection(section: Pair<String, ArrayList<T>>) {
        items.add(section)
        if (section.first != SECTION_HEADER_HIDDEN) sectionCount += 1
        itemCount += section.second.count()
        notifyDataSetChanged()
    }

    /**
     * Update an item in the adapter. This relies on [T] equals() to find an item to replace.
     * @param newItem The item to update in the adapter.
     */
    fun updateItem(newItem: T) {
        var positionInAdapter = 0
        items.forEach { section ->
            section.second.forEachIndexed { index, item ->
                if (item == newItem) {
                    section.second.apply {
                        removeAt(index)
                        add(index, newItem)
                    }
                    notifyItemChanged(positionInAdapter)
                    return
                }
                positionInAdapter += 1
            }
            if (section.first != SECTION_HEADER_HIDDEN) positionInAdapter += 1
        }
    }

    companion object {
        const val ITEM_TYPE_DEFAULT = 0
        const val ITEM_TYPE_SECTION_HEADER = 1

        const val SECTION_HEADER_HIDDEN = ""
    }
}
