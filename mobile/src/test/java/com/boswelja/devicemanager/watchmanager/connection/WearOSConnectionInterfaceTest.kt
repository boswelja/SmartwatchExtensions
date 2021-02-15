package com.boswelja.devicemanager.watchmanager.connection

import android.os.Build
import android.os.Looper.getMainLooper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.TestCapabilityInfo
import com.boswelja.devicemanager.TestNode
import com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.common.connection.Messages.CLEAR_PREFERENCES
import com.boswelja.devicemanager.common.connection.Messages.RESET_APP
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.connection.wearos.WearOSConnectionInterface
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class WearOSConnectionInterfaceTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val coroutineScope = TestCoroutineScope()
    private val transformNodeToWatch: (node: Node) -> Watch = {
        Watch(it.id, it.displayName, WearOSConnectionInterface.PLATFORM)
    }

    private val dummyNodes = setOf<Node>(
        TestNode("id1", "Watch 1"),
        TestNode("id2", "Watch 2"),
        TestNode("id3", "Watch 3")
    )

    private val dummyWatches = dummyNodes.map(transformNodeToWatch)

    @RelaxedMockK private lateinit var capabilityClient: CapabilityClient
    @RelaxedMockK private lateinit var nodeClient: NodeClient
    @RelaxedMockK private lateinit var messageClient: MessageClient
    @RelaxedMockK private lateinit var dataClient: DataClient

    private lateinit var connectionInterface: WearOSConnectionInterface

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `availableWatches contains only the watches that have the app and are connected`() {
        connectionInterface = getConnectionInterface()
        var expectedWatches = dummyWatches
        // Test with matching sets
        connectionInterface.connectedNodes = dummyNodes.toList()
        connectionInterface.nodesWithApp = dummyNodes.toList()
        connectionInterface.dataChanged.fire()
        assertThat(connectionInterface.availableWatches.getOrAwaitValue())
            .containsExactlyElementsIn(expectedWatches)

        // Test with more connected nodes than capable nodes
        val nodesWithApp = dummyNodes.drop(1)
        expectedWatches = nodesWithApp.map(transformNodeToWatch)
        connectionInterface.connectedNodes = dummyNodes.toList()
        connectionInterface.nodesWithApp = nodesWithApp
        connectionInterface.dataChanged.fire()
        assertThat(connectionInterface.availableWatches.getOrAwaitValue())
            .containsExactlyElementsIn(expectedWatches)

        // Test with more capable nodes than connected nodes
        val connectedNodes = dummyNodes.drop(1)
        expectedWatches = connectedNodes.map(transformNodeToWatch)
        connectionInterface.connectedNodes = connectedNodes
        connectionInterface.nodesWithApp = dummyNodes.toList()
        connectionInterface.dataChanged.fire()
        assertThat(connectionInterface.availableWatches.getOrAwaitValue())
            .containsExactlyElementsIn(expectedWatches)

        // Test with no nodes
        connectionInterface.connectedNodes = emptyList()
        connectionInterface.nodesWithApp = emptyList()
        connectionInterface.dataChanged.fire()
        assertThat(connectionInterface.availableWatches.getOrAwaitValue()).isEmpty()
    }

    @Test
    fun `sendMessage passes the request to MessageClient`() {
        connectionInterface = getConnectionInterface()
        val node = dummyNodes.first()
        val dummyWatch = transformNodeToWatch(node)
        val path = "/message-path"
        val data = ByteArray(8)
        connectionInterface.sendMessage(dummyWatch.id, path, data)
        verify(exactly = 1) { messageClient.sendMessage(dummyWatch.id, path, data) }
    }

    @Test
    fun `getWatchStatus returns the correct status`() {
        connectionInterface = getConnectionInterface()
        val dummyNode = dummyNodes.first()
        val dummyWatch = transformNodeToWatch(dummyNode)

        connectionInterface.nodesWithApp = listOf(dummyNode)
        connectionInterface.connectedNodes = listOf(dummyNode)
        assertThat(connectionInterface.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(Watch.Status.CONNECTED)

        connectionInterface.nodesWithApp = listOf(dummyNode)
        connectionInterface.connectedNodes = listOf(dummyNode)
        assertThat(connectionInterface.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(Watch.Status.NOT_REGISTERED)

        connectionInterface.nodesWithApp = emptyList()
        connectionInterface.connectedNodes = listOf(dummyNode)
        assertThat(connectionInterface.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(Watch.Status.MISSING_APP)

        connectionInterface.nodesWithApp = emptyList()
        connectionInterface.connectedNodes = listOf(dummyNode)
        assertThat(connectionInterface.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(Watch.Status.MISSING_APP)

        connectionInterface.nodesWithApp = listOf(dummyNode)
        connectionInterface.connectedNodes = emptyList()
        assertThat(connectionInterface.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(Watch.Status.DISCONNECTED)

        connectionInterface.nodesWithApp = listOf(dummyNode)
        connectionInterface.connectedNodes = emptyList()
        assertThat(connectionInterface.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(Watch.Status.ERROR)

        connectionInterface.nodesWithApp = emptyList()
        connectionInterface.connectedNodes = emptyList()
        assertThat(connectionInterface.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(Watch.Status.ERROR)

        connectionInterface.nodesWithApp = emptyList()
        connectionInterface.connectedNodes = emptyList()
        assertThat(connectionInterface.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(Watch.Status.ERROR)
    }

    @Test
    fun `nodesWithApp is correctly updated on init`() {
        every {
            capabilityClient.getCapability(CAPABILITY_WATCH_APP, any())
        } returns Tasks.forResult(
            TestCapabilityInfo(CAPABILITY_WATCH_APP, dummyNodes.toMutableSet()) as CapabilityInfo
        )

        connectionInterface = getConnectionInterface()

        shadowOf(getMainLooper()).idle()

        assertThat(connectionInterface.nodesWithApp)
            .containsExactlyElementsIn(dummyNodes)
        verify(exactly = 1) { capabilityClient.getCapability(CAPABILITY_WATCH_APP, any()) }
    }

    @Test
    fun `connectedNodes is correctly updated on init`() {
        every { nodeClient.connectedNodes } returns Tasks.forResult(dummyNodes.toList())
        connectionInterface = getConnectionInterface()

        shadowOf(getMainLooper()).idle()

        assertThat(connectionInterface.connectedNodes)
            .containsExactlyElementsIn(dummyNodes)
        verify(exactly = 1) { nodeClient.connectedNodes }
    }

    @Test
    fun `refreshData calls appropriate functions and updates LiveData`() {
        connectionInterface = getConnectionInterface()

        shadowOf(getMainLooper()).idle()

        every { nodeClient.connectedNodes } returns Tasks.forResult(dummyNodes.toList())
        every {
            capabilityClient.getCapability(CAPABILITY_WATCH_APP, any())
        } returns Tasks.forResult(
            TestCapabilityInfo(CAPABILITY_WATCH_APP, dummyNodes.toMutableSet()) as CapabilityInfo
        )

        connectionInterface.refreshData()

        shadowOf(getMainLooper()).idle()

        verify { nodeClient.connectedNodes }
        verify { capabilityClient.getCapability(CAPABILITY_WATCH_APP, any()) }
        assertThat(connectionInterface.availableWatches.getOrAwaitValue())
            .containsExactlyElementsIn(dummyWatches)
    }

    @Test
    fun `updatePreferenceOnWatch does nothing with invalid key`() {
        val key = "non-sync-key"
        connectionInterface = getConnectionInterface()
        connectionInterface.updatePreferenceOnWatch(dummyWatches.first(), key, 0)
        verify(exactly = 0) { dataClient.putDataItem(any()) }
    }

    @Test
    fun `updatePreferenceOnWatch correctly syncs all bool prefs`() {
        connectionInterface = getConnectionInterface()
        SyncPreferences.BOOL_PREFS.forEach {
            connectionInterface.updatePreferenceOnWatch(dummyWatches.first(), it, false)
        }
        verify(exactly = SyncPreferences.BOOL_PREFS.count()) { dataClient.putDataItem(any()) }
    }

    @Test
    fun `updatePreferenceOnWatch correctly syncs all int prefs`() {
        connectionInterface = getConnectionInterface()
        SyncPreferences.INT_PREFS.forEach {
            connectionInterface.updatePreferenceOnWatch(dummyWatches.first(), it, 0)
        }
        verify(exactly = SyncPreferences.INT_PREFS.count()) { dataClient.putDataItem(any()) }
    }

    @Test
    fun `updatePreferenceOnWatch does nothing in invalid data type for key`() {
        connectionInterface = getConnectionInterface()
        SyncPreferences.BOOL_PREFS.forEach {
            connectionInterface.updatePreferenceOnWatch(dummyWatches.first(), it, 0)
        }
        SyncPreferences.INT_PREFS.forEach {
            connectionInterface.updatePreferenceOnWatch(dummyWatches.first(), it, false)
        }
        verify(exactly = 0) { dataClient.putDataItem(any()) }
    }

    @Test
    fun `resetWatchApp sends the reset message to the watch`() {
        val watch = dummyWatches.first()
        connectionInterface = getConnectionInterface()
        connectionInterface.resetWatchApp(watch)
        verify(exactly = 1) { messageClient.sendMessage(watch.id, RESET_APP, null) }
    }

    @Test
    fun `resetWatchPreferences sends the reset message to the watch`() {
        val watch = dummyWatches.first()
        connectionInterface = getConnectionInterface()
        connectionInterface.resetWatchPreferences(watch)
        verify(exactly = 1) { messageClient.sendMessage(watch.id, CLEAR_PREFERENCES, null) }
    }

    private fun getConnectionInterface(): WearOSConnectionInterface {
        return WearOSConnectionInterface(
            capabilityClient,
            nodeClient,
            messageClient,
            dataClient,
            coroutineScope
        )
    }
}
