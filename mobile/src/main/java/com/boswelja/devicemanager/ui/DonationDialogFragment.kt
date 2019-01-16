package com.boswelja.devicemanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
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
        PurchasesUpdatedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingSpinenr: ProgressBar

    private lateinit var billingClient: BillingClient
    private val skus = listOf(
            "donate1",
            "donate2",
            "donate3",
            "donate5",
            "donate10"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_donations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = view.findViewById<TextView>(R.id.title)
        title.text = "Donate"

        val cancelButton = view.findViewById<MaterialButton>(R.id.cancel_btn)
        cancelButton.setOnClickListener {
            dismiss()
        }

        recyclerView = view.findViewById(R.id.donation_rv)
        loadingSpinenr = view.findViewById(R.id.loading_spinner)

        billingClient = BillingClient.newBuilder(view.context).setListener(this).build()
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(responseCode: Int) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            val params = SkuDetailsParams.newBuilder()
                    .setSkusList(skus)
                    .setType(BillingClient.SkuType.INAPP)
            billingClient.querySkuDetailsAsync(params.build(), this)
        }
    }

    override fun onSkuDetailsResponse(responseCode: Int, skuDetailsList: MutableList<SkuDetails>?) {
        if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
            skuDetailsList.sortBy { it.priceAmountMicros }
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.VERTICAL, false)
            recyclerView.adapter = DonationAdapter(skuDetailsList, this)
            loadingSpinenr.visibility = View.GONE

        }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK) dismiss()
    }

    override fun onBillingServiceDisconnected() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dismiss() {
        billingClient.endConnection()
        super.dismiss()
    }
    fun launchBillingFlow(sku: SkuDetails?) {
        val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(sku)
                .build()
        billingClient.launchBillingFlow(activity, flowParams)
}
}