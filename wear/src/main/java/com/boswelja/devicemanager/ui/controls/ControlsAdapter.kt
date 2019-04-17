/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.controls

import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.service.ActionService

class ControlsAdapter : RecyclerView.Adapter<ControlsAdapter.ViewHolder>() {

    private val controls = ControlItems.values()

    override fun getItemCount(): Int {
        return controls.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val control = controls[position]
        holder.icon.setImageResource(control.drawableRes)
        when (control) {
            ControlItems.LockPhone -> {
                holder.label.text = context.getString(control.titleRes)
                holder.itemView.setOnClickListener {
                    val intent = Intent(holder.itemView.context, ActionService::class.java)
                    intent.putExtra(ActionService.EXTRA_ACTION, References.LOCK_PHONE_PATH)
                    holder.itemView.context.startService(intent)
                }
            }
            ControlItems.PhoneBattery -> {
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(holder.itemView.context)
                holder.itemView.setOnClickListener {
                    if (sharedPrefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)) {
                        val intent = Intent(holder.itemView.context, ActionService::class.java)
                        intent.putExtra(ActionService.EXTRA_ACTION, References.REQUEST_BATTERY_UPDATE_PATH)
                        holder.itemView.context.startService(intent)
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener { _, key ->
                    when (key) {
                        PreferenceKey.BATTERY_PERCENT_KEY,
                        PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                            updateBatteryPercent(holder, sharedPrefs)
                        }
                    }
                }
                updateBatteryPercent(holder, sharedPrefs)
            }
        }
    }

    private fun updateBatteryPercent(holder: ViewHolder, sharedPrefs: SharedPreferences) {
        val phoneBattery = sharedPrefs.getInt(PreferenceKey.BATTERY_PERCENT_KEY, 0)
        val batterySyncEnabled = sharedPrefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
        if (phoneBattery > 0) {
            holder.label.text = String.format(holder.itemView.context.getString(R.string.phone_battery_desc), phoneBattery)
        } else if (!batterySyncEnabled) {
            holder.label.text = holder.itemView.context.getString(R.string.battery_sync_disabled)
        } else {
            holder.label.text = holder.itemView.context.getString(R.string.phone_battery_unknown_long)
        }
        holder.icon.drawable.level = phoneBattery
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.model_control, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val label: TextView = itemView.findViewById(R.id.label)
    }
}
