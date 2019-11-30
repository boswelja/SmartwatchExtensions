package com.boswelja.devicemanager.ui.common

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R

class WatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val icon: AppCompatImageView = itemView.findViewById(R.id.icon)
    val topLine: AppCompatTextView = itemView.findViewById(R.id.top_line)
    val bottomLine: AppCompatTextView = itemView.findViewById(R.id.bottom_line)
}