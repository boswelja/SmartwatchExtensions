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

class AppsAdapter(private val fragment: AppManagerFragment) : RecyclerView.Adapter<AppsAdapter.AppItemViewHolder>() {

    private var showSystem: Boolean = false

    private val apps = ArrayList<AppPackageInfo>()

    override fun getItemCount(): Int = count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemViewHolder {
        return AppItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_app_manager_item, parent, false))
    }

    override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
        val context = holder.itemView.context
        val app = getAppInfo(position)
        holder.appNameView.text = app.label
        holder.appDescView.text = app.versionName
        holder.appIconView.setImageDrawable(Utils.getAppIcon(context, app.packageName))
        holder.itemView.setOnClickListener {
            fragment.onItemClick(app)
        }
    }

    private fun getAppInfo(position: Int): AppPackageInfo {
        var app = apps[position]
        if (!showSystem) {
            val filteredApps = apps.filter { !it.isSystemApp }
            app = filteredApps[position]
        }
        return app
    }

    private fun count(): Int {
        return if (showSystem) {
            apps.count()
        } else {
            apps.count { !it.isSystemApp }
        }
    }

    fun setShowSystemApps(show: Boolean) {
        if (showSystem != show) {
            showSystem = show
            notifyDataSetChanged()
        }
    }

    fun add(app: AppPackageInfo) {
        apps.add(app)
        apps.sortBy { it.label }
        val index = apps.indexOf(app)
        notifyItemInserted(index)
    }

    fun remove(packageName: String) {
        val appToRemove = apps.firstOrNull { it.packageName == packageName }
        if (appToRemove != null) {
            val index = apps.indexOf(appToRemove)
            apps.remove(appToRemove)
            notifyItemRemoved(index)
        }
    }

    fun setAllApps(appsToSet: ArrayList<AppPackageInfo>) {
        apps.clear()
        apps.addAll(appsToSet)
        apps.sortBy { it.label }
        notifyDataSetChanged()
    }

    inner class AppItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIconView: AppCompatImageView = itemView.findViewById(R.id.app_icon)
        val appNameView: AppCompatTextView = itemView.findViewById(R.id.app_package_name)
        val appDescView: AppCompatTextView = itemView.findViewById(R.id.app_desc)
    }
}
