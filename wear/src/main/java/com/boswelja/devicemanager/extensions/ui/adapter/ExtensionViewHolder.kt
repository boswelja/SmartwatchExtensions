package com.boswelja.devicemanager.extensions.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.extensions.ui.Extension

abstract class ExtensionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  abstract fun bind(item: Extension)
  abstract fun setOnClickListener(clickCallback: () -> Unit)
}