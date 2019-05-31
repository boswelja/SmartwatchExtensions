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

    private val userApps = ArrayList<AppPackageInfo>()
    private val systemApps = ArrayList<AppPackageInfo>()

    override fun getItemCount(): Int {
        val userAppsCount = userApps.count()
        val systemAppsCount = systemApps.count()

        return if (userAppsCount > 0 && systemAppsCount > 0)
            1 + userAppsCount + 1 + systemAppsCount
        else if (userAppsCount > 0 && systemAppsCount == 0)
            1 + userAppsCount + 1
        else if (userAppsCount == 0 && systemAppsCount > 0)
            1 + systemAppsCount
        else
            0
    }

    override fun getItemViewType(position: Int): Int {
        val userAppsCount = userApps.count()
        val systemAppsCount = systemApps.count()

        if (userAppsCount > 0 && systemAppsCount > 0) {
            return when (position) {
                0 -> VIEW_TYPE_USER_APPS_SECTION
                userAppsCount + 1 -> VIEW_TYPE_SYSTEM_APPS_SECTION
                else -> VIEW_TYPE_APP_ITEM
            }
        } else if (userAppsCount > 0 && systemAppsCount == 0) {
            return when (position) {
                0 -> VIEW_TYPE_USER_APPS_SECTION
                else -> VIEW_TYPE_APP_ITEM
            }
        } else if (userAppsCount == 0 && systemAppsCount > 0) {
            return if (position == 0)
                VIEW_TYPE_SYSTEM_APPS_SECTION
            else
                VIEW_TYPE_APP_ITEM
        }

        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER_APPS_SECTION,
            VIEW_TYPE_SYSTEM_APPS_SECTION -> {
                SectionHeader(LayoutInflater.from(parent.context).inflate(R.layout.common_recyclerview_section_header, parent, false))
            }
            else -> {
                AppItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_app_manager_item, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context
        when (holder) {
            is SectionHeader -> {
                when (getItemViewType(position)) {
                    VIEW_TYPE_USER_APPS_SECTION -> holder.sectionHeaderTextView.text = context.getString(R.string.app_manager_section_user_apps)
                    VIEW_TYPE_SYSTEM_APPS_SECTION -> holder.sectionHeaderTextView.text = context.getString(R.string.app_manager_section_system_apps)
                }
                if (position == 0) {
                    holder.sectionHeaderDivider.visibility = View.INVISIBLE
                }
            }
            is AppItemViewHolder -> {
                val app = getApp(position)
                holder.appNameView.text = app.packageLabel
                holder.appDescView.text = app.versionName
                holder.appIconView.setImageDrawable(Utils.getAppIcon(context, app.packageName))
                holder.itemView.setOnClickListener {
                    fragment.onItemClick(app)
                }
            }
        }
    }

    private fun getApp(position: Int): AppPackageInfo {
        return if (position <= userApps.count()) {
            userApps[position - 1]
        } else {
            systemApps[position - userApps.count() - 2]
        }
    }

    fun add(app: AppPackageInfo) {
        if (userApps.firstOrNull { it.packageName == app.packageName } == null) {
            userApps.add(app)
            userApps.sortBy { it.packageLabel }
            val index = userApps.indexOf(app)
            notifyItemInserted(index)
        }
    }

    fun remove(packageName: String) {
        val index = userApps.indexOfFirst { it.packageName == packageName }
        if (index > -1) {
            userApps.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getFromPackageName(packageName: String): AppPackageInfo? {
        var app = userApps.firstOrNull { it.packageName == packageName }
        if (app == null) {
            app = systemApps.firstOrNull { it.packageName == packageName }
        }
        return app
    }

    fun setAllApps(appsToSet: AppPackageInfoList) {
        userApps.clear()
        systemApps.clear()
        userApps.addAll(appsToSet.filterNot {it.isSystemApp}.toTypedArray().sortedBy { it.packageLabel })
        systemApps.addAll(appsToSet.filter {it.isSystemApp}.toTypedArray().sortedBy {it.packageLabel})
        notifyDataSetChanged()
    }

    inner class AppItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIconView: AppCompatImageView = itemView.findViewById(R.id.app_icon)
        val appNameView: AppCompatTextView = itemView.findViewById(R.id.app_package_name)
        val appDescView: AppCompatTextView = itemView.findViewById(R.id.app_desc)
    }

    inner class SectionHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sectionHeaderDivider: View = itemView.findViewById(R.id.divider)
        val sectionHeaderTextView: AppCompatTextView = itemView.findViewById(R.id.section_header_text)
    }

    companion object {
        private const val VIEW_TYPE_USER_APPS_SECTION = 0
        private const val VIEW_TYPE_SYSTEM_APPS_SECTION = 1
        private const val VIEW_TYPE_APP_ITEM = 2
    }
}
