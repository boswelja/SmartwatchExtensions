/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.MainOption
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.complications.ActionService

class MainAdapter(private val options: ArrayList<MainOption>) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return options.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.label.text = option.label
        holder.icon.setImageResource(option.iconRes)
        holder.itemView?.setOnClickListener {
            when (option.type) {
                References.TYPE_LOCK_PHONE -> {
                    val intent = Intent(holder.itemView.context, ActionService::class.java)
                    intent.putExtra(References.INTENT_ACTION_EXTRA, References.LOCK_PHONE_PATH)
                    holder.itemView.context.startService(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.model_main_option, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val label: TextView = itemView.findViewById(R.id.label)
    }
}
