/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.donate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseDialogFragment
import com.boswelja.devicemanager.ui.main.MainActivity
import com.google.android.material.button.MaterialButton

class DonationDialogFragment :
        BaseDialogFragment(),
        BillingClientStateListener,
        PurchasesUpdatedListener,
        ConsumeResponseListener {

    private var billingConnectionRetryCounter = 0

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var cancelBtn: MaterialButton

    private lateinit var billingClient: BillingClient
    private val skus = listOf(
            "donation_small",
            "donation_medium",
            "donation_large"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_donations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelBtn = view.findViewById(R.id.cancel_btn)
        cancelBtn.setOnClickListener {
            dismiss()
        }

        recyclerView = view.findViewById(R.id.donation_rv)
        loadingSpinner = view.findViewById(R.id.loading_spinner)

        billingClient = BillingClient.newBuilder(view.context)
                .setListener(this)
                .enablePendingPurchases()
                .build()
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val params = SkuDetailsParams.newBuilder()
                    .setSkusList(skus)
                    .setType(BillingClient.SkuType.INAPP)
            billingClient.querySkuDetailsAsync(params.build()) { skuResult, skuDetailsList ->
                if (skuResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    skuDetailsList.sortBy { it.priceAmountMicros }
                    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false)
                    recyclerView.adapter = DonationAdapter(skuDetailsList, this)
                    setLoading(false)
                } else {
                    dismiss()
                    (activity as MainActivity).createSnackBar(getString(R.string.donation_failed_message))
                }
                val purchaseQuery = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                if (purchaseQuery.billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                        !purchaseQuery.purchasesList.isNullOrEmpty()) {
                    handlePurchases(purchaseQuery.purchasesList)
                }
            }
        } else {
            dismiss()
            (activity as MainActivity).createSnackBar(getString(R.string.donation_failed_message))
        }
    }

    override fun onBillingServiceDisconnected() {
        if (billingConnectionRetryCounter < 3) {
            billingConnectionRetryCounter++
            billingClient.startConnection(this)
        } else {
            dismiss()
            (activity as MainActivity).createSnackBar(getString(R.string.donation_connection_failed_message))
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
                && !purchases.isNullOrEmpty()) {
            handlePurchases(purchases)
        } else {
            dismiss()
            (activity as MainActivity).createSnackBar(getString(R.string.donation_failed_message))
        }
    }

    override fun onConsumeResponse(billingResult: BillingResult, purchaseToken: String?) {
        dismiss()
        (activity as MainActivity).createSnackBar(getString(R.string.donation_processed_message))
    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        setLoading(true)
        for (purchase in purchases) {
            if (!purchase.isAcknowledged) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    val consumePurchaseParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                    billingClient.consumeAsync(consumePurchaseParams, this)
                } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                    dismiss()
                    (activity as MainActivity).createSnackBar(getString(R.string.donation_pending_message))
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            recyclerView.visibility = View.INVISIBLE
            loadingSpinner.visibility = View.VISIBLE
            cancelBtn.visibility = View.INVISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            loadingSpinner.visibility = View.GONE
            cancelBtn.visibility = View.VISIBLE
        }
    }

    fun launchBillingFlow(sku: SkuDetails?) {
        setLoading(true)
        val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(sku)
                .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }
}