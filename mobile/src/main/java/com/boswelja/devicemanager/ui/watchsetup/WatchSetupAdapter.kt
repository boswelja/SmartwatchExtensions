package com.boswelja.devicemanager.ui.watchsetup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.common.WatchViewHolder
import com.boswelja.devicemanager.watchconnectionmanager.Watch

class WatchSetupAdapter(private val watchSetupFragment: WatchSetupFragment) : RecyclerView.Adapter<WatchViewHolder>() {

    private val watches = ArrayList<Watch>()

    override fun getItemCount(): Int = watches.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.common_recyclerview_item_icon_two_line, parent, false)
        return WatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: WatchViewHolder, position: Int) {
        val watch = watches[position]
        holder.icon.setImageResource(R.drawable.ic_watch)
        holder.topLine.text = watch.name
        holder.bottomLine.text = watch.id
        holder.itemView.setOnClickListener {
            watchSetupFragment.requestRegisterWatch(watch)
        }
    }

    fun setWatches(newWatches: List<Watch>) {
        watches.clear()
        watches.addAll(newWatches)
        notifyDataSetChanged()
    }
}