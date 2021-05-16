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
import com.boswelja.smartwatchextensions.AppState
import com.boswelja.smartwatchextensions.appStateStore
import com.boswelja.smartwatchextensions.donate.Skus
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
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _clientConnected.postValue(true)
                    updateOneTimeSkuDetails()
                    updateRecurringSkuDetails()
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
    private val _hasDonated = MutableLiveData(false)

    val oneTimeDonations: LiveData<List<SkuDetails>>
        get() = _oneTimeDonations
    val recurringDonations: LiveData<List<SkuDetails>>
        get() = _recurringDonations
    val clientConnected: LiveData<Boolean>
        get() = _clientConnected
    val hasDonated: LiveData<Boolean>
        get() = _hasDonated

    init {
        startClientConnection()
    }

    override fun onCleared() {
        super.onCleared()
        if (clientConnected.value == true) billingClient.endConnection()
    }

    private fun startClientConnection() {
        billingClient.startConnection(clientStateListener)
    }

    private fun updateOneTimeSkuDetails() {
        val oneTimeSkuDetailParams = SkuDetailsParams.newBuilder()
            .setSkusList(Skus.ALL_ONE_TIME)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        billingClient.querySkuDetailsAsync(oneTimeSkuDetailParams) { _, skus ->
            val skuDetails = skus ?: emptyList()
            _oneTimeDonations.postValue(skuDetails.sortedBy { it.priceAmountMicros })
        }
    }

    private fun updateRecurringSkuDetails() {
        val recurringSkuDetailParams = SkuDetailsParams.newBuilder()
            .setSkusList(Skus.ALL_RECURRING)
            .setType(BillingClient.SkuType.SUBS)
            .build()
        billingClient.querySkuDetailsAsync(recurringSkuDetailParams) { _, skus ->
            val skuDetails = skus ?: emptyList()
            _recurringDonations.postValue(skuDetails.sortedBy { it.priceAmountMicros })
        }
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
}
