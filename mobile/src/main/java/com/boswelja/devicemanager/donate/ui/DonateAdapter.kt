/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.donate.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.android.billingclient.api.SkuDetails
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.recyclerview.item.IconTwoLineViewHolder
import com.boswelja.devicemanager.donate.SkuDetailDiffer

class DonateAdapter(private val clickCallback: (sku: SkuDetails) -> Unit) :
    ListAdapter<SkuDetails, IconTwoLineViewHolder>(SkuDetailDiffer()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconTwoLineViewHolder {
    return IconTwoLineViewHolder.from(parent)
  }

  override fun onBindViewHolder(holder: IconTwoLineViewHolder, position: Int) {
    val sku = getItem(position)
    holder.bind(R.drawable.pref_ic_donate, sku.title, sku.price)
    holder.itemView.setOnClickListener {
      clickCallback(sku)
    }
  }
}
