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

class AppsAdapter(private val fragment: AppManagerFragment) : RecyclerView.Adapter<AppsAdapter.AppItemViewHolder>() {

    private val apps = ArrayList<AppPackageInfo>()

    override fun getItemCount(): Int = count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemViewHolder {
        return AppItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_app_manager_item, parent, false))
    }

    override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
        val context = holder.itemView.context
        val app = apps[position]
        holder.appNameView.text = app.packageLabel
        holder.appDescView.text = app.versionName
        holder.appIconView.setImageDrawable(Utils.getAppIcon(context, app.packageName))
        holder.itemView.setOnClickListener {
            fragment.onItemClick(app)
        }
    }

    private fun count(): Int = apps.count()

    fun add(app: AppPackageInfo) {
        if (apps.firstOrNull { it.packageName == app.packageName } == null) {
            apps.add(app)
            apps.sortBy { it.packageLabel }
            val index = apps.indexOf(app)
            notifyItemInserted(index)
        }
    }

    fun remove(packageName: String) {
        val index = apps.indexOfFirst { it.packageName == packageName }
        if (index > -1) {
            apps.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getFromPackageName(packageName: String): AppPackageInfo? {
        return apps.firstOrNull { it.packageName == packageName }
    }

    fun setAllApps(appsToSet: AppPackageInfoList) {
        apps.clear()
        apps.addAll(appsToSet)
        apps.sortBy { it.packageLabel }
        notifyDataSetChanged()
    }

    inner class AppItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIconView: AppCompatImageView = itemView.findViewById(R.id.app_icon)
        val appNameView: AppCompatTextView = itemView.findViewById(R.id.app_package_name)
        val appDescView: AppCompatTextView = itemView.findViewById(R.id.app_desc)
    }
}
