/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.donate.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.DonateItemBinding
import com.boswelja.devicemanager.donate.SkuDetailDiffer

class DonateAdapter(private val clickCallback: (sku: SkuDetails) -> Unit) :
    ListAdapter<SkuDetails, DonateAdapter.DonateViewHolder>(SkuDetailDiffer()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonateViewHolder {
        return DonateViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DonateViewHolder, position: Int) {
        val skuDetails = getItem(position)

        val drawableRes = when {
            skuDetails.sku.contains("large") -> R.drawable.ic_donate_large
            else -> R.drawable.ic_donate_small
        }
        holder.bind(drawableRes, skuDetails.title, skuDetails.description)
        holder.itemView.setOnClickListener { clickCallback(skuDetails) }
    }

    class DonateViewHolder internal constructor(
        private val binding: DonateItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(@DrawableRes icon: Int, title: String, desc: String) {
            binding.icon.setImageResource(icon)
            binding.title.text = title
            binding.desc.text = desc
        }

        companion object {
            fun from(parent: ViewGroup): DonateViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DonateItemBinding.inflate(layoutInflater, parent, false)
                return DonateViewHolder(binding)
            }
        }
    }
}
