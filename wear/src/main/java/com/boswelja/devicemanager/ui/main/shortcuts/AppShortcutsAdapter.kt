package com.boswelja.devicemanager.ui.main.shortcuts

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.settings.SettingsActivity

class AppShortcutsAdapter : RecyclerView.Adapter<AppShortcutsAdapter.ViewHolder>() {

    private val items = AppShortcut.values()

    override fun getItemCount(): Int = items.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.common_item_small, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shortcut = items[position]
        holder.iconView.setImageResource(shortcut.iconRes)
        holder.labelView.setText(shortcut.labelRes)
        holder.itemView.setOnClickListener {
            when (shortcut) {
                AppShortcut.Settings -> {
                    Intent(it.context, SettingsActivity::class.java).also { intent ->
                        it.context.startActivity(intent)
                    }
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconView: AppCompatImageView = itemView.findViewById(R.id.icon_view)
        val labelView: AppCompatTextView = itemView.findViewById(R.id.label_view)
    }
}