package com.boswelja.devicemanager.capability

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.common.connection.Capability.MANAGE_APPS
import com.boswelja.devicemanager.common.connection.Capability.RECEIVE_DND
import com.boswelja.devicemanager.common.connection.Capability.SEND_DND
import com.boswelja.devicemanager.common.connection.Capability.SYNC_BATTERY
import com.google.android.gms.wearable.CapabilityClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R, Build.VERSION_CODES.O, Build.VERSION_CODES.N_MR1])
class CapabilityUpdaterTest {

    @RelaxedMockK lateinit var capabilityClient: CapabilityClient

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
    fun `updateSendBattery enables SYNC_BATTERY capability`() {
        capabilityUpdater.updateSendBattery()
        verify(exactly = 1) { capabilityClient.addLocalCapability(SYNC_BATTERY.name) }
    }

    @Test
    fun `updateSendDnD enables SEND_DND capability`() {
        capabilityUpdater.updateSendDnD()
        verify(exactly = 1) { capabilityClient.addLocalCapability(SEND_DND.name) }
    }

    @Test
    fun `updateReceiveDnD updates RECEIVE_DND correctly`() {
        // If SDK is old enough to not need permission, capability should be added.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            capabilityUpdater.updateReceiveDnD()
            verify(exactly = 1) { capabilityClient.addLocalCapability(RECEIVE_DND.name) }
        } else {
            // Emulate permission granted
            every { capabilityUpdater.hasNotiPolicyAccess() } returns true
            capabilityUpdater.updateReceiveDnD()
            verify(exactly = 1) { capabilityClient.addLocalCapability(RECEIVE_DND.name) }

            // Emulate permission denied
            every { capabilityUpdater.hasNotiPolicyAccess() } returns false
            capabilityUpdater.updateReceiveDnD()
            verify(exactly = 1) { capabilityClient.removeLocalCapability(RECEIVE_DND.name) }
        }
    }

    @Test
    fun `updateManageApps updates MANAGE_APPS correctly`() {
        // If SDK is old enough to not need permission, capability should be added.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            capabilityUpdater.updateManageApps()
            verify(exactly = 1) { capabilityClient.addLocalCapability(MANAGE_APPS.name) }
        } else {
            // Emulate permission granted
            every { capabilityUpdater.canQueryAllPackages() } returns true
            capabilityUpdater.updateManageApps()
            verify(exactly = 1) { capabilityClient.addLocalCapability(MANAGE_APPS.name) }

            // Emulate permission denied
            every { capabilityUpdater.canQueryAllPackages() } returns false
            capabilityUpdater.updateManageApps()
            verify(exactly = 1) { capabilityClient.removeLocalCapability(MANAGE_APPS.name) }
        }
    }
}
