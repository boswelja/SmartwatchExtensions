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

  private val purchasesUpdatedListener = PurchasesUpdatedListener { _, purchases ->
    purchases?.forEach {
      consumePurchase(it)
    }
  }

  private val clientStateListener = object : BillingClientStateListener {
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

  private val billingClient = BillingClient.newBuilder(application)
      .setListener(purchasesUpdatedListener)
      .enablePendingPurchases()
      .build()

  private val _clientConnected = MutableLiveData(false)
  val clientConnected: LiveData<Boolean>
    get() = _clientConnected

  private val _skus = MutableLiveData<List<SkuDetails>?>(emptyList())
  val skus: LiveData<List<SkuDetails>?>
    get() = _skus

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
      val params = SkuDetailsParams.newBuilder()
          .setSkusList(Skus.all)
          .setType(BillingClient.SkuType.INAPP)
          .build()
      val skuDetails = billingClient.querySkuDetails(params)
      if (skuDetails.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
        _skus.postValue(skuDetails.skuDetailsList ?: emptyList())
      } else {
        _skus.postValue(null)
      }
    }
  }

  private fun consumePurchase(purchase: Purchase) {
    coroutineScope.launch {
      if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.consumePurchase(params)
      }
    }
  }

  fun launchBillingFlow(activity: Activity, sku: SkuDetails) {
    val params = BillingFlowParams.newBuilder()
        .setSkuDetails(sku)
        .build()
    billingClient.launchBillingFlow(activity, params)
  }
}