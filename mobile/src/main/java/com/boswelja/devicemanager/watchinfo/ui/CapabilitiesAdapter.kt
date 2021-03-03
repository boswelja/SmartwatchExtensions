package com.boswelja.devicemanager.watchinfo.ui

import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.boswelja.devicemanager.common.connection.Capability
import com.boswelja.devicemanager.common.ui.OneLineViewHolder

/**
 * An adapter for presenting a list of [Capability] to the user.
 */
class CapabilitiesAdapter : ListAdapter<Capability, OneLineViewHolder>(CapabilityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneLineViewHolder {
        val holder = OneLineViewHolder.from(parent)
        holder.itemView.updatePaddingRelative(start = 0, end = 0)
        return holder
    }

    override fun onBindViewHolder(holder: OneLineViewHolder, position: Int) {
        val capability = getItem(position)
        holder.bind(holder.itemView.context.getString(capability.label))
    }

    class CapabilityDiffCallback : DiffUtil.ItemCallback<Capability>() {
        override fun areContentsTheSame(oldItem: Capability, newItem: Capability): Boolean {
            return oldItem.label == newItem.label
        }

        override fun areItemsTheSame(oldItem: Capability, newItem: Capability): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
