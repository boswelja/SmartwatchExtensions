package com.boswelja.smartwatchextensions.donate.ui

import android.app.Activity
import android.app.Application
import androidx.datastore.core.DataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.querySkuDetails
import com.boswelja.smartwatchextensions.AppState
import com.boswelja.smartwatchextensions.appStateStore
import com.boswelja.smartwatchextensions.donate.Skus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class DonateViewModel internal constructor(
    application: Application,
    private val dataStore: DataStore<AppState>
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        application.appStateStore
    )

    private val oneTimeSkuDetailParams = SkuDetailsParams.newBuilder()
        .setSkusList(Skus.ALL_ONE_TIME)
        .setType(BillingClient.SkuType.INAPP)
        .build()

    private val recurringSkuDetailParams = SkuDetailsParams.newBuilder()
        .setSkusList(Skus.ALL_RECURRING)
        .setType(BillingClient.SkuType.SUBS)
        .build()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { _, purchases ->
        purchases?.forEach {
            if (it.sku in Skus.ALL_RECURRING) {
                acknowledgeRecurringPurchase(it)
            } else {
                consumeOneTimePurchase(it)
            }
        }
    }

    private val clientStateListener =
        object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                _clientConnected.tryEmit(
                    result.responseCode == BillingClient.BillingResponseCode.OK
                )
            }

            override fun onBillingServiceDisconnected() {
                _clientConnected.tryEmit(false)
            }
        }

    private val billingClient =
        BillingClient.newBuilder(application)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

    private val _clientConnected = MutableStateFlow(false)
    private val _hasDonated = MutableLiveData(false)

    val clientConnected: Flow<Boolean>
        get() = _clientConnected
    val hasDonated: LiveData<Boolean>
        get() = _hasDonated

    init {
        startClientConnection()
    }

    override fun onCleared() {
        super.onCleared()
        billingClient.endConnection()
    }

    private fun startClientConnection() {
        billingClient.startConnection(clientStateListener)
    }

    private fun consumeOneTimePurchase(purchase: Purchase) {
        Timber.d("consumeOneTimePurchase($purchase) called")
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        viewModelScope.launch {
            val result = billingClient.consumePurchase(params)
            if (result.purchaseToken != null) {
                _hasDonated.postValue(true)
                dataStore.updateData {
                    it.copy(hasDonated = true)
                }
            }
        }
    }

    private fun acknowledgeRecurringPurchase(purchase: Purchase) {
        Timber.d("acknowledgeRecurringPurchase($purchase) called")
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        viewModelScope.launch {
            val result = billingClient.acknowledgePurchase(params)
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _hasDonated.postValue(true)
                dataStore.updateData {
                    it.copy(hasDonated = true)
                }
            }
        }
    }

    fun launchBillingFlow(activity: Activity, sku: SkuDetails) {
        val params = BillingFlowParams.newBuilder().setSkuDetails(sku).build()
        billingClient.launchBillingFlow(activity, params)
    }

    fun oneTimeDonations(): Flow<List<SkuDetails>?> = _clientConnected.map { connected ->
        if (connected) {
            val details = billingClient.querySkuDetails(oneTimeSkuDetailParams)
            details.skuDetailsList
        } else {
            null
        }
    }

    fun recurringDonations(): Flow<List<SkuDetails>?> = _clientConnected.map { connected ->
        if (connected) {
            val details = billingClient.querySkuDetails(recurringSkuDetailParams)
            details.skuDetailsList
        } else {
            null
        }
    }
}
