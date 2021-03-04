/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.donate.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.querySkuDetails
import com.boswelja.devicemanager.donate.Skus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DonateViewModel(application: Application) : AndroidViewModel(application) {

    private val coroutineJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + coroutineJob)

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { _, purchases -> purchases?.forEach { consumePurchase(it) } }

    private val clientStateListener =
        object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _clientConnected.postValue(true)
                    querySkuDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                _clientConnected.postValue(false)
            }
        }

    private val billingClient =
        BillingClient.newBuilder(application)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

    private val _clientConnected = MutableLiveData(false)
    private val _oneTimeDonations = MutableLiveData<List<SkuDetails>>(emptyList())
    private val _recurringDonations = MutableLiveData<List<SkuDetails>>(emptyList())

    val oneTimeDonations: LiveData<List<SkuDetails>>
        get() = _oneTimeDonations
    val recurringDonations: LiveData<List<SkuDetails>>
        get() = _recurringDonations
    val clientConnected: LiveData<Boolean>
        get() = _clientConnected

    init {
        connectClient()
    }

    override fun onCleared() {
        super.onCleared()
        if (clientConnected.value == true) billingClient.endConnection()
    }

    private fun connectClient() {
        billingClient.startConnection(clientStateListener)
    }

    private fun querySkuDetails() {
        coroutineScope.launch {
            val params =
                SkuDetailsParams.newBuilder()
                    .setSkusList(Skus.ALL_ONE_TIME)
                    .setType(BillingClient.SkuType.INAPP)
                    .build()
            val skuDetails = billingClient.querySkuDetails(params)
            if (skuDetails.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _oneTimeDonations.postValue(skuDetails.skuDetailsList ?: emptyList())
            } else {
                _oneTimeDonations.postValue(emptyList())
            }
        }
    }

    private fun consumePurchase(purchase: Purchase) {
        coroutineScope.launch {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                val params =
                    ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                billingClient.consumePurchase(params)
            }
        }
    }

    fun launchBillingFlow(activity: Activity, sku: SkuDetails) {
        val params = BillingFlowParams.newBuilder().setSkuDetails(sku).build()
        billingClient.launchBillingFlow(activity, params)
    }
}
