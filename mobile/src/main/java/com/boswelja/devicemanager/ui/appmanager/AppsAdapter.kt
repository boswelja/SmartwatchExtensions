package com.boswelja.devicemanager.ui.appmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo

class AppsAdapter : RecyclerView.Adapter<AppsAdapter.AppItemViewHolder>() {

    private val apps = ArrayList<AppPackageInfo>()

    override fun getItemCount(): Int = apps.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemViewHolder {
        return AppItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_app_manager_item, parent, false))
    }

    override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
        val app = apps[position]
        holder.appIconView.setImageDrawable(app.packageIcon)
        holder.appNameView.text = app.packageName
        holder.appDescView.text = app.versionName
    }

    fun add(app: AppPackageInfo) {
        apps.add(app)
        apps.sortBy { it.packageName }
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

    inner class AppItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIconView: AppCompatImageView = itemView.findViewById(R.id.app_icon)
        val appNameView: AppCompatTextView = itemView.findViewById(R.id.app_package_name)
        val appDescView: AppCompatTextView = itemView.findViewById(R.id.app_desc)
    }
}