package com.boswelja.devicemanager.appmanager.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.ui.IconTwoLineViewHolder

class AppsAdapter(private val clickCallback: (App) -> Unit) :
    ListAdapter<App, IconTwoLineViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconTwoLineViewHolder {
        return IconTwoLineViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: IconTwoLineViewHolder, position: Int) {
        val app = getItem(position)
        if (app.icon?.bitmap != null) {
            holder.bind(app.icon!!.bitmap, app.label, app.version)
        } else {
            holder.bind(R.drawable.android_head, app.label, app.version)
        }
        holder.itemView.setOnClickListener { clickCallback(app) }
    }
}
