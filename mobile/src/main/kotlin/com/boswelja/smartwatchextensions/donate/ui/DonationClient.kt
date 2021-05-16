package com.boswelja.smartwatchextensions.donate.ui

import android.app.Activity
import android.app.Application
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
import com.boswelja.smartwatchextensions.appStateStore
import com.boswelja.smartwatchextensions.donate.Skus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A wrapper class for [BillingClient] to simplify making purchase requests.
 */
class DonationClient(application: Application) {

    private val stateStore = application.appStateStore

    private val purchaseResultChannel = Channel<Boolean>()

    private val coroutineJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + coroutineJob)

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        coroutineScope.launch {
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases?.forEach {
                    val ackResult = if (it.isAutoRenewing) {
                        acknowledgeRecurringPurchase(it)
                    } else {
                        consumeOneTimePurchase(it)
                    }
                    handlePurchaseResult(ackResult)
                }
            } else {
                purchaseResultChannel.send(false)
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

    private val oneTimeSkuDetailParams = SkuDetailsParams.newBuilder()
        .setSkusList(Skus.ALL_ONE_TIME)
        .setType(BillingClient.SkuType.INAPP)
        .build()

    private val recurringSkuDetailParams = SkuDetailsParams.newBuilder()
        .setSkusList(Skus.ALL_RECURRING)
        .setType(BillingClient.SkuType.SUBS)
        .build()

    private val billingClient =
        BillingClient.newBuilder(application)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

    private val _clientConnected = MutableStateFlow(false)
    val clientConnected: Flow<Boolean>
        get() = _clientConnected

    init {
        billingClient.startConnection(clientStateListener)
    }

    private suspend fun consumeOneTimePurchase(purchase: Purchase): BillingResult {
        Timber.d("consumeOneTimePurchase($purchase) called")
        val params = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        return billingClient.consumePurchase(params).billingResult
    }

    private suspend fun acknowledgeRecurringPurchase(purchase: Purchase): BillingResult {
        Timber.d("acknowledgeRecurringPurchase($purchase) called")
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        return billingClient.acknowledgePurchase(params)
    }

    private suspend fun handlePurchaseResult(result: BillingResult) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            stateStore.updateData {
                it.copy(hasDonated = true)
            }
            purchaseResultChannel.send(true)
        } else {
            purchaseResultChannel.send(false)
        }
    }

    /**
     * Get a [Flow] of all one-time donations.
     */
    fun oneTimeDonations(): Flow<List<SkuDetails>?> = _clientConnected.map { connected ->
        if (connected) {
            val details = billingClient.querySkuDetails(oneTimeSkuDetailParams)
            details.skuDetailsList
        } else {
            null
        }
    }

    /**
     * Get a [Flow] of all recurring (monthly) donations.
     */
    fun recurringDonations(): Flow<List<SkuDetails>?> = _clientConnected.map { connected ->
        if (connected) {
            val details = billingClient.querySkuDetails(recurringSkuDetailParams)
            details.skuDetailsList
        } else {
            null
        }
    }

    /**
     * Launch a billing flow and collect the result.
     * @param activity [Activity].
     * @param sku The [SkuDetails] for the donation to launch the flow for.
     * @return true if the user successfully donated, false otherwise.
     */
    suspend fun tryDonate(activity: Activity, sku: SkuDetails): Boolean {
        // Make the request
        val params = BillingFlowParams.newBuilder().setSkuDetails(sku).build()
        val launchResult = billingClient.launchBillingFlow(activity, params)

        return if (launchResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // Wait for the result
            purchaseResultChannel.receive()
        } else {
            // There was a problem launching
            false
        }
    }

    /**
     * Clean up the attached [BillingClient] and other resources.
     */
    fun destroy() {
        coroutineJob.cancel()
        billingClient.endConnection()
    }
}
