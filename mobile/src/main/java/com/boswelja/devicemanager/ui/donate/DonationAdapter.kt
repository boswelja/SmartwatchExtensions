/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.donate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.boswelja.devicemanager.R

class DonationAdapter(private val skus: List<SkuDetails>, private val dialogFragment: DonationDialogFragment) : RecyclerView.Adapter<DonationAdapter.DonationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.dialog_donations_item, parent, false)
        return DonationViewHolder(view)
    }

    override fun getItemCount(): Int = skus.count()

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        val sku = skus[position]
        var title = sku.title
        val stringEnd = title.indexOf("(")
        title = title.substring(0, stringEnd).trim()
        holder.donationName.text = title
        holder.donationAmount.text = sku.price
        holder.itemView.setOnClickListener {
            dialogFragment.launchBillingFlow(sku)
        }
    }

    inner class DonationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val donationName: AppCompatTextView = itemView.findViewById(R.id.donation_name)
        val donationAmount: AppCompatTextView = itemView.findViewById(R.id.donation_amount)
    }
}