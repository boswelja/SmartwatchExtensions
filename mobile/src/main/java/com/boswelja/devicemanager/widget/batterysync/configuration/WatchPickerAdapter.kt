package com.boswelja.devicemanager.widget.batterysync.configuration

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.ui.common.WatchViewHolder
import com.boswelja.devicemanager.watchconnectionmanager.Watch

class WatchPickerAdapter(private val watches: List<Watch>, private val activity: WatchBatteryWidgetConfigurationActivity) : RecyclerView.Adapter<WatchViewHolder>() {

    override fun getItemCount(): Int = watches.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchViewHolder {
        return WatchViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_recyclerview_item_icon_two_line, parent, false))
    }

    override fun onBindViewHolder(holder: WatchViewHolder, position: Int) {
        val watch = watches[position]
        holder.icon.setImageResource(R.drawable.ic_watch)
        holder.topLine.text = watch.name
        holder.bottomLine.text = if (watch.boolPrefs[BATTERY_SYNC_ENABLED_KEY] == true) {
            "Tap to create widget"
        } else {
            "Battery Sync disabled for this watch"
        }
        holder.itemView.setOnClickListener {
            if (watch.boolPrefs[BATTERY_SYNC_ENABLED_KEY] == true) {
                activity.finishAndCreateWidget(watch.id)
            }
        }
    }

}