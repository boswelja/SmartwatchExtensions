package com.boswelja.devicemanager.ui.appmanager

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo

class AppsAdapter : RecyclerView.Adapter<AppsAdapter.AppItemViewHolder>() {

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
        holder.appDescView.text = app.isSystemApp.toString()
        try {
            val icon = context.packageManager.getApplicationIcon(app.packageName)
            holder.appIconView.setImageDrawable(icon)
        } catch (_: PackageManager.NameNotFoundException) {
            holder.appIconView.setImageResource(R.drawable.ic_app_icon_missing)
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