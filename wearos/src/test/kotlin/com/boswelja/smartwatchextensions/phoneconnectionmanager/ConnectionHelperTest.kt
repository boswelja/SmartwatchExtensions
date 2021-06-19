package com.boswelja.smartwatchextensions.phoneconnectionmanager

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.phoneconnectionmanager.ConnectionHelper.Companion.CAPABILITY_PHONE_APP
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class ConnectionHelperTest {

    @MockK private lateinit var capabilityClient: CapabilityClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `phoneStatus flows DISCONNECTED correctly`() {
        every {
            capabilityClient.getCapability(CAPABILITY_PHONE_APP, any())
        } returns Tasks.forResult(createCapabilityInfoFor(Status.DISCONNECTED))

        val connectionHelper = ConnectionHelper(capabilityClient, 5000L)

        val status = runBlocking {
            withTimeout(TIMEOUT) {
                connectionHelper.phoneStatus().first()
            }
        }

        expectThat(status).isEqualTo(Status.DISCONNECTED)
    }

    @Test
    fun `phoneStatus flows CONNECTED correctly`() {
        every {
            capabilityClient.getCapability(CAPABILITY_PHONE_APP, any())
        } returns Tasks.forResult(createCapabilityInfoFor(Status.CONNECTED))

        val connectionHelper = ConnectionHelper(capabilityClient, 5000L)

        val status = runBlocking {
            withTimeout(TIMEOUT) {
                connectionHelper.phoneStatus().first()
            }
        }

        expectThat(status).isEqualTo(Status.CONNECTED)
    }

    @Test
    fun `phoneStatus flows CONNECTED_NEARBY correctly`() {
        every {
            capabilityClient.getCapability(CAPABILITY_PHONE_APP, any())
        } returns Tasks.forResult(createCapabilityInfoFor(Status.CONNECTED_NEARBY))

        val connectionHelper = ConnectionHelper(capabilityClient, 5000L)

        val status = runBlocking {
            withTimeout(TIMEOUT) {
                connectionHelper.phoneStatus().first()
            }
        }

        expectThat(status).isEqualTo(Status.CONNECTED_NEARBY)
    }

    private fun createCapabilityInfoFor(status: Status): CapabilityInfo {
        val nodes = if (status == Status.DISCONNECTED) {
            mutableSetOf<Node>()
        } else {
            mutableSetOf(
                object : Node {
                    override fun getDisplayName(): String = "Phone"
                    override fun getId(): String = "phone"
                    override fun isNearby(): Boolean = status == Status.CONNECTED_NEARBY
                }
            )
        }
        return object : CapabilityInfo {
            override fun getName(): String = CAPABILITY_PHONE_APP
            override fun getNodes(): MutableSet<out Node> = nodes
        }
    }

    companion object {
        const val TIMEOUT = 500L
    }
}
