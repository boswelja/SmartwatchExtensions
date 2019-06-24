/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
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
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.common.appmanager.AppPackageInfoList

class AppsAdapter(private val fragment: AppManagerFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = ArrayList<AppSection>()

    private val fallbackIcon = fragment.context?.getDrawable(R.drawable.ic_app_icon_unknown)

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
        return when (viewType) {
            VIEW_TYPE_SECTION -> {
                SectionHeader(LayoutInflater.from(parent.context).inflate(R.layout.common_recyclerview_section_header, parent, false))
            }
            else -> {
                AppItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_recyclerview_item_icon_two_line, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context
        when (holder) {
            is SectionHeader -> {
                val section = getSection(position)
                holder.sectionHeaderTextView.text = context.getString(section?.sectionTitleRes!!)
                if (position == 0) {
                    holder.sectionHeaderDivider.visibility = View.INVISIBLE
                }
            }
            is AppItemViewHolder -> {
                val app = getApp(position)
                if (app != null) {
                    holder.appNameView.text = app.packageLabel
                    holder.appDescView.text = app.versionName
                    holder.appIconView.setImageDrawable(Utils.getAppIcon(context, app.packageName, fallbackIcon))
                    holder.itemView.setOnClickListener {
                        fragment.onItemClick(app)
                    }
                }
            }
        }
    }

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

    fun getFromPackageName(packageName: String): AppPackageInfo? {
        for (section in data) {
            val apps = section.appsInSection
            val app = apps.firstOrNull { it.packageName == packageName }
            if (app != null) return app
        }
        return null
    }

    fun setAllApps(appsToSet: AppPackageInfoList) {
        data.clear()
        val userApps = ArrayList(appsToSet.filterNot { it.isSystemApp }.toTypedArray().sortedBy { it.packageLabel })
        val systemApps = ArrayList(appsToSet.filter { it.isSystemApp }.toTypedArray().sortedBy { it.packageLabel })
        val userAppsSection = AppSection(R.string.app_manager_section_user_apps, userApps)
        val systemAppsSection = AppSection(R.string.app_manager_section_system_apps, systemApps)
        data.add(userAppsSection)
        data.add(systemAppsSection)
        notifyDataSetChanged()
    }

    inner class AppItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIconView: AppCompatImageView = itemView.findViewById(R.id.icon)
        val appNameView: AppCompatTextView = itemView.findViewById(R.id.top_line)
        val appDescView: AppCompatTextView = itemView.findViewById(R.id.bottom_line)
    }

    inner class SectionHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sectionHeaderDivider: View = itemView.findViewById(R.id.divider)
        val sectionHeaderTextView: AppCompatTextView = itemView.findViewById(R.id.section_header_text)
    }

    companion object {
        private const val VIEW_TYPE_SECTION = 0
        private const val VIEW_TYPE_ITEM = 1
    }
}
