/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.recyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.common.recyclerview.item.SectionHeaderItem
import java.util.Locale
import kotlin.collections.ArrayList

/**
 * A custom [RecyclerView.Adapter] set up for sections.
 * @property T The item type to display.
 * @param items An [ArrayList] of [Pair] objects that correspond to sections. Each pair should
 * contain a [String] for the section label (or [SECTION_HEADER_HIDDEN] if there shouldn't be a label), and
 * an [ArrayList] of items to show in the section.
 * @param showSectionDividers Whether section headers should show a divider as well.
 */
abstract class SectionedAdapter<T>(
    protected val items: ArrayList<Pair<String, ArrayList<T>>> = ArrayList(),
    private val showSectionDividers: Boolean = true,
    private val sectionSortMode: SortMode = SortMode.DEFAULT,
    private val itemSortMode: SortMode = SortMode.DEFAULT
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var layoutInflater: LayoutInflater? = null

    private var sectionCount: Int = 0
    private var itemCount: Int = 0

    init {
        updateSectionCount()
        updateItemCount()

        sortSections()
        sortItems()
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
                    if (position == 0) {
                        dividerView.visibility = View.INVISIBLE
                    }
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
     * Gets the adapter position of an item in a given section at a given position in said section.
     * @param section The index of the section the item is in.
     * @param positionInSection The index of the item in the section.
     * @return The position of the item in the adapter.
     */
    private fun getAdapterPosition(section: Int, positionInSection: Int): Int {
        var adapterPosition = 0
        items.forEachIndexed { index, pair ->
            if (pair.first != SECTION_HEADER_HIDDEN) adapterPosition += 1
            if (index == section) {
                adapterPosition += positionInSection
                return@forEachIndexed
            } else {
                adapterPosition += pair.second.count()
            }
        }
        return adapterPosition
    }

    /**
     * Sorts all the sections based on [sectionSortMode]
     */
    private fun sortSections() {
        when (sectionSortMode) {
            SortMode.ASCENDING -> {
                items.sortBy { it.first.toLowerCase(Locale.getDefault()) }
            }
            SortMode.DESCENDING -> {
                items.sortByDescending { it.first.toLowerCase(Locale.getDefault()) }
            }
            SortMode.DEFAULT -> return
        }
    }

    /**
     * Sorts all items in each section based on [itemSortMode].
     */
    private fun sortItems() {
        when (itemSortMode) {
            SortMode.ASCENDING,
            SortMode.DESCENDING
            -> {
                items.indices.forEach {
                    sortSectionItems(it)
                }
            }
            SortMode.DEFAULT -> return
        }
    }

    /**
     * Sorts all items in a given section based on [itemSortMode].
     * @param sectionIndex The index of the section to sort.
     */
    private fun sortSectionItems(sectionIndex: Int) {
        when (itemSortMode) {
            SortMode.ASCENDING -> {
                items[sectionIndex].second.sortBy { item -> item.toString().toLowerCase(Locale.getDefault()) }
            }
            SortMode.DESCENDING -> {
                items[sectionIndex].second.sortByDescending { item -> item.toString().toLowerCase(Locale.getDefault()) }
            }
            SortMode.DEFAULT -> return
        }
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

        sortSections()
        sortItems()

        notifyDataSetChanged()
    }

    /**
     * Adds a new section to the data set and notifies the adapter.
     * @param section The [Pair] representing the new section.
     */
    fun addSection(section: Pair<String, ArrayList<T>>) {
        items.add(section)
        val sectionItemsCount = section.second.count()
        if (section.first != SECTION_HEADER_HIDDEN) sectionCount += 1
        itemCount += sectionItemsCount

        var adapterPosition = getAdapterPosition(items.indexOf(section), 0)
        var itemCount = sectionItemsCount
        if (section.first != SECTION_HEADER_HIDDEN) {
            adapterPosition -= 1
            itemCount += 1
        }

        sortSections()
        sortSectionItems(items.indexOf(section))

        notifyItemRangeInserted(adapterPosition, itemCount)
    }

    /**
     * Update an item in the adapter. This relies on [T] equals() to find an item to replace.
     * @param newItem The item to update in the adapter.
     */
    fun updateItem(newItem: T) {
        var positionInAdapter = 0
        items.forEachIndexed { sectionIndex, section ->
            section.second.forEachIndexed { index, item ->
                if (item == newItem) {
                    section.second.apply {
                        removeAt(index)
                        add(index, newItem)
                    }
                    sortSectionItems(sectionIndex)
                    notifyItemChanged(positionInAdapter)
                    return
                }
                positionInAdapter += 1
            }
            if (section.first != SECTION_HEADER_HIDDEN) positionInAdapter += 1
        }
    }

    /**
     * Add an item to a given section.
     * @param sectionIndex The index of the section to add the item to.
     * @param item The new item of type [T] to add to the section.
     */
    fun addItem(sectionIndex: Int, item: T) {
        items[sectionIndex].second.apply {
            add(item)
        }.also {
            sortSectionItems(sectionIndex)
            notifyItemInserted(getAdapterPosition(sectionIndex, it.indexOf(item)))
        }
    }

    /**
     * Remove an item from the adapter.
     * @param item The item of type [T] to remove from the adapter.
     * @param sectionIndex The index of the section to remove the item from, or -1 if it's not known.
     */
    fun removeItem(item: T, sectionIndex: Int = -1) {
        val realSectionIndex = if (sectionIndex == -1) {
            items.indexOfFirst { it.second.contains(item) }
        } else {
            sectionIndex
        }
        val itemIndexInSection = items[realSectionIndex].second.indexOf(item)
        items[realSectionIndex].second.removeAt(itemIndexInSection)
        notifyItemRemoved(getAdapterPosition(realSectionIndex, itemIndexInSection))
    }

    /**
     * Defines available sorting modes the adapter can use.
     * [DEFAULT] disables sorting and sticks with the default order of the items list.
     */
    enum class SortMode {
        DEFAULT,
        ASCENDING,
        DESCENDING
    }

    companion object {
        const val ITEM_TYPE_DEFAULT = 0
        const val ITEM_TYPE_SECTION_HEADER = 1

        const val SECTION_HEADER_HIDDEN = ""
    }
}
