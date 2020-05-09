/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.common.appmanager.AppPackageInfoList
import com.boswelja.devicemanager.common.recyclerview.item.IconTwoLineItem
import com.boswelja.devicemanager.common.recyclerview.item.SectionHeaderItem

class AppsAdapter(private val fragment: AppManagerFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = ArrayList<AppSection>()
    private val fallbackIcon = fragment.context?.getDrawable(R.drawable.ic_app_icon_unknown)

    private var layoutInflater: LayoutInflater? = null

    override fun getItemCount(): Int {
        var count = 0
        for (section in data) {
            count += section.countIncludingHeader()
        }
        return count
    }

    override fun getItemViewType(position: Int): Int {
        var currentSectionStart = 0
        for (section in data) {
            if (position == currentSectionStart) {
                return VIEW_TYPE_SECTION
            } else {
                val currentSectionLength = currentSectionStart + section.count()
                if (position in (currentSectionStart)..currentSectionLength) {
                    return VIEW_TYPE_ITEM
                } else {
                    currentSectionStart += section.countIncludingHeader()
                }
            }
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SECTION -> {
                SectionHeaderItem.create(layoutInflater!!, parent)
            }
            else -> {
                IconTwoLineItem.create(layoutInflater!!, parent)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context
        when (holder) {
            is SectionHeaderItem -> {
                val section = getSection(position)
                holder.textView.text = context.getString(section?.sectionTitleRes!!)
                if (position == 0) {
                    holder.dividerView.visibility = View.INVISIBLE
                }
            }
            is IconTwoLineItem -> {
                val app = getApp(position)
                if (app != null) {
                    holder.topTextView.text = app.packageLabel
                    holder.bottomTextView.text = app.versionName
                    holder.iconView.setImageDrawable(Utils.getAppIcon(context, app.packageName, fallbackIcon))
                    holder.itemView.setOnClickListener {
                        fragment.launchAppInfoActivity(app)
                    }
                }
            }
        }
    }

    /**
     * Gets the [AppPackageInfo] at a given position.
     * @param position The position of the target [AppPackageInfo]
     * @return The [AppPackageInfo] at the given position, null if it doesn't exist
     */
    private fun getApp(position: Int): AppPackageInfo? {
        var currentSectionStart = 0
        for (section in data) {
            val currentSectionEnd = (currentSectionStart + section.countIncludingHeader())
            if (position in currentSectionStart..currentSectionEnd) {
                return section.appsInSection[position - currentSectionStart - 1]
            }
            currentSectionStart = currentSectionEnd
        }
        return null
    }

    /**
     * Gets the [AppSection] at a given position.
     * @param position The position of the target [AppSection]
     * @return The [AppSection] at the given position, null if it doesn't exist
     */
    private fun getSection(position: Int): AppSection? {
        var currentSectionEnd = 0
        for (section in data) {
            currentSectionEnd += section.countIncludingHeader()
            if (position < currentSectionEnd) {
                return section
            }
        }
        return null
    }

    /**
     * Add an [AppPackageInfo] to the adapter.
     * @param app The [AppPackageInfo] to add.
     */
    fun add(app: AppPackageInfo) {
        for (section in data) {
            val apps = section.appsInSection
            if (apps[0].isSystemApp == app.isSystemApp) {
                apps.add(app)
                apps.sortBy { it.packageLabel }
                val index = apps.indexOf(app)
                notifyItemInserted(index)
            }
        }
    }

    /**
     * Remove an [AppPackageInfo] from the adapter.
     * @param packageName The package name of the [AppPackageInfo] to remove.
     */
    fun remove(packageName: String) {
        for (section in data) {
            val apps = section.appsInSection
            val index = apps.indexOfFirst { it.packageName == packageName }
            if (index > 0) {
                apps.removeAt(index)
                notifyItemRemoved(index + 1)
            }
        }
    }

    /**
     * Gets an [AppPackageInfo] from the adapter that matches a given package name.
     * @param packageName The package name to match against.
     * @return The matching [AppPackageInfo], null if none.
     */
    fun getFromPackageName(packageName: String): AppPackageInfo? {
        for (section in data) {
            val apps = section.appsInSection
            val app = apps.firstOrNull { it.packageName == packageName }
            if (app != null) return app
        }
        return null
    }

    /**
     * Sorts and applies a new [AppPackageInfoList] to the adapter.
     * @param appsToSet The [AppPackageInfoList] to sort and set to the adapter.
     */
    fun setAllApps(appsToSet: AppPackageInfoList) {
        val userApps = ArrayList(appsToSet.filterNot { it.isSystemApp }.toTypedArray().sortedBy { it.packageLabel })
        val systemApps = ArrayList(appsToSet.filter { it.isSystemApp }.toTypedArray().sortedBy { it.packageLabel })
        val userAppsSection = AppSection(R.string.app_manager_section_user_apps, userApps)
        val systemAppsSection = AppSection(R.string.app_manager_section_system_apps, systemApps)
        data.apply {
            clear()
            add(userAppsSection)
            add(systemAppsSection)
        }
        notifyDataSetChanged()
    }

    companion object {
        private const val VIEW_TYPE_SECTION = 0
        private const val VIEW_TYPE_ITEM = 1
    }
}
