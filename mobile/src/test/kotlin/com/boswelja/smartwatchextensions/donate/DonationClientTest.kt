package com.boswelja.smartwatchextensions.donate

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.boswelja.smartwatchextensions.AppState
import com.boswelja.smartwatchextensions.appStateStore
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class DonationClientTest {

    private val appState = MutableStateFlow(AppState())

    private lateinit var purchaseListener: PurchasesUpdatedListener
    private lateinit var clientStateListener: BillingClientStateListener
    private lateinit var donationClient: DonationClient

    @RelaxedMockK private lateinit var appStateStore: DataStore<AppState>
    @MockK private lateinit var billingClientBuilder: BillingClient.Builder
    @MockK private lateinit var billingClient: BillingClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this@DonationClientTest)

        // Mock state store
        runBlocking { appState.emit(AppState()) }
        mockkStatic(Context::appStateStore)
        every { appStateStore.data } returns appState
        every { any<Context>().appStateStore } returns appStateStore

        // Mock billing client
        mockkStatic(BillingClient::class)
        every { billingClientBuilder.setListener(any()) } answers {
            purchaseListener = firstArg() as PurchasesUpdatedListener
            billingClientBuilder
        }
        every { billingClientBuilder.enablePendingPurchases() } returns billingClientBuilder
        every { billingClient.startConnection(any()) } answers {
            clientStateListener = firstArg() as BillingClientStateListener
        }
        every { billingClient.endConnection() } just Runs
        every { billingClientBuilder.build() } returns billingClient
        every { BillingClient.newBuilder(any()) } returns billingClientBuilder

        // Create donation client
        donationClient = DonationClient(ApplicationProvider.getApplicationContext())
        verify { BillingClient.newBuilder(any()) }
        verify { billingClient.startConnection(any()) }
    }

    @Test
    fun `clientConnected is true when BillingClient init succeeds`(): Unit = runBlocking {
        val result = createBillingResult(BillingClient.BillingResponseCode.OK)
        // Artificially fire client state connected
        clientStateListener.onBillingSetupFinished(result)

        expectThat(donationClient.clientConnected.first()).isTrue()
    }

    @Test
    fun `clientConnected is false when BillingClient init fails`(): Unit = runBlocking {
        val result = createBillingResult(BillingClient.BillingResponseCode.ERROR)
        // Artificially fire client state connected
        clientStateListener.onBillingSetupFinished(result)

        expectThat(donationClient.clientConnected.first()).isFalse()
    }

    @Test
    fun `tryDonate returns false when billing flow fails to start`(): Unit = runBlocking {
        // Mock a failure
        every {
            billingClient.launchBillingFlow(any(), any())
        } returns createBillingResult(BillingClient.BillingResponseCode.ERROR)

        // Make the call and validate
        val result = donationClient.tryDonate(mockk(), createSkuDetails())
        expectThat(result).isFalse()
    }

    @Test
    fun `tryDonate returns false when the user cancels the flow`(): Unit = runBlocking {
        // Mock the billing flow
        val purchaseResult = createBillingResult(BillingClient.BillingResponseCode.USER_CANCELED)
        val purchases = createPurchases(false)
        every {
            billingClient.launchBillingFlow(any(), any())
        } answers {
            purchaseListener.onPurchasesUpdated(purchaseResult, purchases)
            createBillingResult(BillingClient.BillingResponseCode.OK)
        }

        // Make the call and validate
        val result = donationClient.tryDonate(mockk(), createSkuDetails())
        expectThat(result).isFalse()
    }

    @Test
    fun `tryDonate returns false when there is an error processing purchases`(): Unit =
        runBlocking {
            // Mock the billing flow
            val purchaseResult = createBillingResult(BillingClient.BillingResponseCode.ERROR)
            val purchases = createPurchases(false)
            every {
                billingClient.launchBillingFlow(any(), any())
            } answers {
                purchaseListener.onPurchasesUpdated(purchaseResult, purchases)
                createBillingResult(BillingClient.BillingResponseCode.OK)
            }

            // Make the call and validate
            val result = donationClient.tryDonate(mockk(), createSkuDetails())
            expectThat(result).isFalse()
        }

    @Test
    fun `tryDonate consumes successful one time purchases`(): Unit = runBlocking {
        // Mock the billing flow
        val purchaseResult = createBillingResult(BillingClient.BillingResponseCode.OK)
        val purchases = createPurchases(false)
        every {
            billingClient.launchBillingFlow(any(), any())
        } answers {
            purchaseListener.onPurchasesUpdated(purchaseResult, purchases)
            createBillingResult(BillingClient.BillingResponseCode.OK)
        }
        every {
            billingClient.consumeAsync(any(), any())
        } answers {
            (secondArg() as ConsumeResponseListener).onConsumeResponse(
                createBillingResult(BillingClient.BillingResponseCode.OK),
                ""
            )
        }

        // Make the call and validate
        donationClient.tryDonate(mockk(), createSkuDetails())
        verify { billingClient.consumeAsync(any(), any()) }
    }

    @Test
    fun `tryDonate acknowledges successful recurring purchases`(): Unit = runBlocking {
        // Mock the billing flow
        val purchaseResult = createBillingResult(BillingClient.BillingResponseCode.OK)
        val purchases = createPurchases(true)
        every {
            billingClient.launchBillingFlow(any(), any())
        } answers {
            purchaseListener.onPurchasesUpdated(purchaseResult, purchases)
            createBillingResult(BillingClient.BillingResponseCode.OK)
        }
        every {
            billingClient.acknowledgePurchase(any(), any())
        } answers {
            (secondArg() as AcknowledgePurchaseResponseListener).onAcknowledgePurchaseResponse(
                createBillingResult(BillingClient.BillingResponseCode.OK)
            )
        }

        // Make the call and validate
        donationClient.tryDonate(mockk(), createSkuDetails())
        verify { billingClient.acknowledgePurchase(any(), any()) }
    }

    @Test
    fun `tryDonate returns true when successfully making a recurring purchase`(): Unit =
        runBlocking {
            // Mock the billing flow
            val purchaseResult = createBillingResult(BillingClient.BillingResponseCode.OK)
            val purchases = createPurchases(true)
            every {
                billingClient.launchBillingFlow(any(), any())
            } answers {
                purchaseListener.onPurchasesUpdated(purchaseResult, purchases)
                createBillingResult(BillingClient.BillingResponseCode.OK)
            }
            every {
                billingClient.acknowledgePurchase(any(), any())
            } answers {
                (secondArg() as AcknowledgePurchaseResponseListener).onAcknowledgePurchaseResponse(
                    createBillingResult(BillingClient.BillingResponseCode.OK)
                )
            }

            // Make the call and validate
            val result = donationClient.tryDonate(mockk(), createSkuDetails())
            expectThat(result).isTrue()
        }

    @Test
    fun `tryDonate returns false when failing to make a recurring purchase`(): Unit =
        runBlocking {
            // Mock the billing flow
            val purchaseResult = createBillingResult(BillingClient.BillingResponseCode.OK)
            val purchases = createPurchases(true)
            every {
                billingClient.launchBillingFlow(any(), any())
            } answers {
                purchaseListener.onPurchasesUpdated(purchaseResult, purchases)
                createBillingResult(BillingClient.BillingResponseCode.OK)
            }
            every {
                billingClient.acknowledgePurchase(any(), any())
            } answers {
                (secondArg() as AcknowledgePurchaseResponseListener).onAcknowledgePurchaseResponse(
                    createBillingResult(BillingClient.BillingResponseCode.ERROR)
                )
            }

            // Make the call and validate
            val result = donationClient.tryDonate(mockk(), createSkuDetails())
            expectThat(result).isFalse()
        }

    @Test
    fun `tryDonate returns true when successfully making a one-time purchase`(): Unit =
        runBlocking {
            // Mock the billing flow
            val purchaseResult = createBillingResult(BillingClient.BillingResponseCode.OK)
            val purchases = createPurchases(false)
            every {
                billingClient.launchBillingFlow(any(), any())
            } answers {
                purchaseListener.onPurchasesUpdated(purchaseResult, purchases)
                createBillingResult(BillingClient.BillingResponseCode.OK)
            }
            every {
                billingClient.consumeAsync(any(), any())
            } answers {
                (secondArg() as ConsumeResponseListener).onConsumeResponse(
                    createBillingResult(BillingClient.BillingResponseCode.OK),
                    ""
                )
            }

            // Make the call and validate
            val result = donationClient.tryDonate(mockk(), createSkuDetails())
            expectThat(result).isTrue()
        }

    @Test
    fun `tryDonate returns false when failing to make a one-time purchase`(): Unit =
        runBlocking {
            // Mock the billing flow
            val purchaseResult = createBillingResult(BillingClient.BillingResponseCode.OK)
            val purchases = createPurchases(false)
            every {
                billingClient.launchBillingFlow(any(), any())
            } answers {
                purchaseListener.onPurchasesUpdated(purchaseResult, purchases)
                createBillingResult(BillingClient.BillingResponseCode.OK)
            }
            every {
                billingClient.consumeAsync(any(), any())
            } answers {
                (secondArg() as ConsumeResponseListener).onConsumeResponse(
                    createBillingResult(BillingClient.BillingResponseCode.ERROR),
                    ""
                )
            }

            // Make the call and validate
            val result = donationClient.tryDonate(mockk(), createSkuDetails())
            expectThat(result).isFalse()
        }

    @Test
    fun `destroy ends BillingClient connection`() {
        donationClient.destroy()
        verify { billingClient.endConnection() }
    }

    private fun createBillingResult(
        responseCode: Int
    ): BillingResult {
        val result = mockk<BillingResult>()
        every { result.responseCode } returns responseCode
        every { result.debugMessage } returns "Can't lock me out"
        return result
    }

    private fun createPurchases(isAutoRenewing: Boolean, count: Int = 1): List<Purchase> {
        return (0..count).map {
            val purchase = mockk<Purchase>()
            every { purchase.isAutoRenewing } returns isAutoRenewing
            every { purchase.purchaseToken } returns "token"
            purchase
        }
    }

    private fun createSkuDetails(): SkuDetails {
        val skuDetails = mockk<SkuDetails>()
        every { skuDetails.zza() } returns "sku"
        return skuDetails
    }
}
