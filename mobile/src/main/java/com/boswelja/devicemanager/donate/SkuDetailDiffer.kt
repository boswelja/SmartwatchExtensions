package com.boswelja.devicemanager.donate

import androidx.recyclerview.widget.DiffUtil
import com.android.billingclient.api.SkuDetails

class SkuDetailDiffer : DiffUtil.ItemCallback<SkuDetails>() {

  override fun areItemsTheSame(oldItem: SkuDetails, newItem: SkuDetails): Boolean {
    return oldItem.sku == newItem.sku
  }

  override fun areContentsTheSame(oldItem: SkuDetails, newItem: SkuDetails): Boolean {
    return oldItem == newItem
  }
}