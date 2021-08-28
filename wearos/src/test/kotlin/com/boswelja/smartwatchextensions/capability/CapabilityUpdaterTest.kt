package com.boswelja.smartwatchextensions.capability

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.common.connection.Capability.MANAGE_APPS
import com.boswelja.smartwatchextensions.common.connection.Capability.RECEIVE_DND
import com.boswelja.smartwatchextensions.common.connection.Capability.SEND_DND
import com.boswelja.smartwatchextensions.common.connection.Capability.SYNC_BATTERY
import com.boswelja.smartwatchextensions.runBlockingTimeout
import com.boswelja.watchconnection.wearos.discovery.DiscoveryClient
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.spyk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val Timeout = 2000L

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R, Build.VERSION_CODES.O, Build.VERSION_CODES.N_MR1])
class CapabilityUpdaterTest {

    @RelaxedMockK lateinit var capabilityClient: DiscoveryClient

    private lateinit var capabilityUpdater: CapabilityUpdater

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        capabilityUpdater = spyk(
            CapabilityUpdater(
                ApplicationProvider.getApplicationContext(),
                capabilityClient
            )
        )
    }

    @Test
    fun `updateSendBattery enables SYNC_BATTERY capability`(): Unit = runBlockingTimeout(Timeout) {
        capabilityUpdater.updateSendBattery()
        coVerify(exactly = 1) { capabilityClient.addCapability(SYNC_BATTERY.name) }
    }

    @Test
    fun `updateSendDnD enables SEND_DND capability`(): Unit = runBlockingTimeout(Timeout) {
        capabilityUpdater.updateSendDnD()
        coVerify(exactly = 1) { capabilityClient.addCapability(SEND_DND.name) }
    }

    @Test
    fun `updateReceiveDnD updates RECEIVE_DND correctly`(): Unit = runBlockingTimeout(Timeout) {
        // If SDK is old enough to not need permission, capability should be added.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            capabilityUpdater.updateReceiveDnD()
            coVerify(exactly = 1) { capabilityClient.addCapability(RECEIVE_DND.name) }
        } else {
            // Emulate permission granted
            every { capabilityUpdater.hasNotiPolicyAccess() } returns true
            capabilityUpdater.updateReceiveDnD()
            coVerify(exactly = 1) { capabilityClient.addCapability(RECEIVE_DND.name) }

            // Emulate permission denied
            every { capabilityUpdater.hasNotiPolicyAccess() } returns false
            capabilityUpdater.updateReceiveDnD()
            coVerify(exactly = 1) { capabilityClient.removeCapability(RECEIVE_DND.name) }
        }
    }

    @Test
    fun `updateManageApps updates MANAGE_APPS correctly`(): Unit = runBlockingTimeout(Timeout) {
        // If SDK is old enough to not need permission, capability should be added.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            capabilityUpdater.updateManageApps()
            coVerify(exactly = 1) { capabilityClient.addCapability(MANAGE_APPS.name) }
        } else {
            // Emulate permission granted
            every { capabilityUpdater.canQueryAllPackages() } returns true
            capabilityUpdater.updateManageApps()
            coVerify(exactly = 1) { capabilityClient.addCapability(MANAGE_APPS.name) }

            // Emulate permission denied
            every { capabilityUpdater.canQueryAllPackages() } returns false
            capabilityUpdater.updateManageApps()
            coVerify(exactly = 1) { capabilityClient.removeCapability(MANAGE_APPS.name) }
        }
    }
}
