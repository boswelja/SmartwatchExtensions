/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.MainOption
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.service.ActionService

class MainAdapter(private val options: ArrayList<MainOption>) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return options.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        when (option.type) {
            MainOption.Type.LOCK_PHONE -> {
                holder.label.text = option.label
                holder.icon.setImageResource(option.iconRes)
                holder.itemView.setOnClickListener {
                    val intent = Intent(holder.itemView.context, ActionService::class.java)
                    intent.putExtra(ActionService.INTENT_ACTION_EXTRA, References.LOCK_PHONE_PATH)
                    holder.itemView.context.startService(intent)
                }
            }
            MainOption.Type.PHONE_BATTERY -> {
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(holder.itemView.context)
                holder.itemView.setOnClickListener {
                    if (sharedPrefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)) {
                        val intent = Intent(holder.itemView.context, ActionService::class.java)
                        intent.putExtra(ActionService.INTENT_ACTION_EXTRA, References.REQUEST_BATTERY_UPDATE_PATH)
                        holder.itemView.context.startService(intent)
                    }
                }
                sharedPrefs.registerOnSharedPreferenceChangeListener { _, key ->
                    when (key) {
                        PreferenceKey.BATTERY_PERCENT_KEY -> {
                            updateBatteryPercent(holder, sharedPrefs)
                        }
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
        val phoneBattery = sharedPrefs.getInt(PreferenceKey.BATTERY_PERCENT_KEY, -1)
        val batterySyncEnabled = sharedPrefs.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)
        if (phoneBattery > -1) {
            holder.label.text = String.format(holder.itemView.context.getString(R.string.phone_battery_desc), phoneBattery)
        } else if (!batterySyncEnabled) {
            holder.label.text = holder.itemView.context.getString(R.string.battery_sync_disabled)
        } else {
            holder.label.text = holder.itemView.context.getString(R.string.phone_battery_unknown_long)
        }
        val drawable = holder.icon.context.getDrawable(R.drawable.ic_phone_battery)!!
        drawable.level = phoneBattery
        holder.icon.setImageDrawable(drawable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.model_main_option, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val label: TextView = itemView.findViewById(R.id.label)
    }
}
