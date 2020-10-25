/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
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
