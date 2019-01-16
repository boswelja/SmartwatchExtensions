package com.boswelja.devicemanager.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.boswelja.devicemanager.R
import com.google.android.material.button.MaterialButton

class DonationDialogFragment :
        DialogFragment(),
        BillingClientStateListener,
        SkuDetailsResponseListener,
        PurchasesUpdatedListener,
        ConsumeResponseListener{

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var cancelBtn: MaterialButton

    private lateinit var billingClient: BillingClient
    private val skus = listOf(
            "donate1",
            "donate2",
            "donate3",
            "donate5",
            "donate10"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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

        billingClient = BillingClient.newBuilder(view.context).setListener(this).build()
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(responseCode: Int) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            val params = SkuDetailsParams.newBuilder()
                    .setSkusList(skus)
                    .setType(BillingClient.SkuType.INAPP)
            billingClient.querySkuDetailsAsync(params.build(), this)

            // Consume any previous purchases
            val purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
            if (purchases.responseCode == BillingClient.BillingResponse.OK && purchases.purchasesList != null) {
                for (purchase in purchases.purchasesList) {
                    billingClient.consumeAsync(purchase.purchaseToken, null)
                }
            }
        } else {
            dismiss()
            (activity as MainActivity).createSnackbar(getString(R.string.donation_failed))
        }
    }

    override fun onSkuDetailsResponse(responseCode: Int, skuDetailsList: MutableList<SkuDetails>?) {
        if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
            skuDetailsList.sortBy { it.priceAmountMicros }
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false)
            recyclerView.adapter = DonationAdapter(skuDetailsList, this)
            setLoading(false)

        } else {
            dismiss()
            (activity as MainActivity).createSnackbar(getString(R.string.donation_failed))
        }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            setLoading(true)
            for (purchase in purchases) {
                billingClient.consumeAsync(purchase.purchaseToken, this)
            }

        } else {
            dismiss()
            (activity as MainActivity).createSnackbar(getString(R.string.donation_failed))
        }
    }

    override fun onConsumeResponse(responseCode: Int, purchaseToken: String?) {
        dismiss()
        (activity as MainActivity).createSnackbar(getString(R.string.donation_processed))
    }

    override fun onBillingServiceDisconnected() {
        dismiss()
    }

    override fun dismiss() {
        billingClient.endConnection()
        super.dismiss()
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