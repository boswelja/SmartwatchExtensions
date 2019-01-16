package com.boswelja.devicemanager.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.boswelja.devicemanager.R

class DonationAdapter(private val skus: List<SkuDetails>, private val dialogFragment: DonationDialogFragment) : RecyclerView.Adapter<DonationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_single_line_text, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return skus.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sku = skus[position]
        holder.text.text = sku.price
        holder.itemView.setOnClickListener {
            dialogFragment.launchBillingFlow(sku)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.text)
    }
}