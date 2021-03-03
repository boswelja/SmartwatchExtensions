package com.boswelja.devicemanager.watchmanager.connection.wearos

import android.os.Build
import android.os.Looper.getMainLooper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.TasksAwaitRule
import com.boswelja.devicemanager.TestCapabilityInfo
import com.boswelja.devicemanager.TestNode
import com.boswelja.devicemanager.common.connection.Capability
import com.boswelja.devicemanager.common.connection.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.mockk.verifyAll
import kotlin.experimental.and
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

    @get:Rule
    val tasksAwaitRule = TasksAwaitRule()

    private val coroutineScope = TestCoroutineScope()
    private val transformNodeToWatch: (node: Node) -> Watch = {
        Watch(it.id, it.displayName, WearOSConnectionInterface.PLATFORM)
    }

    private val dummyNodes = listOf<Node>(
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
        connectionInterface = WearOSConnectionInterface(
            capabilityClient,
            nodeClient,
            messageClient,
            dataClient,
            coroutineScope
        )

        shadowOf(getMainLooper()).idle()

        // Verify any existing calls
        verifyAll { capabilityClient.getCapability(any(), any()) }
        verifyAll { nodeClient.connectedNodes }
        confirmVerified()
    }

    @Test
    fun `availableWatches contains only the watches that have the app and are connected`() {
        var expectedWatches = dummyWatches
        // Test with matching sets
        connectionInterface.connectedNodes = dummyNodes
        connectionInterface.nodesWithApp = dummyNodes
        connectionInterface.dataChanged.fire()
        assertThat(connectionInterface.availableWatches.getOrAwaitValue())
            .containsExactlyElementsIn(expectedWatches)

        // Test with more connected nodes than capable nodes
        val nodesWithApp = dummyNodes.drop(1)
        expectedWatches = nodesWithApp.map(transformNodeToWatch)
        connectionInterface.connectedNodes = dummyNodes
        connectionInterface.nodesWithApp = nodesWithApp
        connectionInterface.dataChanged.fire()
        assertThat(connectionInterface.availableWatches.getOrAwaitValue())
            .containsExactlyElementsIn(expectedWatches)

        // Test with more capable nodes than connected nodes
        val connectedNodes = dummyNodes.drop(1)
        expectedWatches = connectedNodes.map(transformNodeToWatch)
        connectionInterface.connectedNodes = connectedNodes
        connectionInterface.nodesWithApp = dummyNodes
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
        val node = dummyNodes.first()
        val dummyWatch = transformNodeToWatch(node)
        val path = "/message-path"
        val data = ByteArray(8)
        connectionInterface.sendMessage(dummyWatch.id, path, data)
        verify(exactly = 1) { messageClient.sendMessage(dummyWatch.id, path, data) }
    }

    @Test
    fun `getWatchStatus returns the correct status`() {
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
    fun `refreshConnectedNodes calls appropriate functions and updates data`() {
        every { nodeClient.connectedNodes } returns Tasks.forResult(dummyNodes)

        connectionInterface.refreshConnectedNodes()

        // Verify connected nodes were checked
        verify { nodeClient.connectedNodes }

        // Verify data updated
        assertThat(connectionInterface.connectedNodes).containsExactlyElementsIn(dummyNodes)
    }

    @Test
    fun `refreshNodesWithApp calls appropriate functions and updates data`() {
        every {
            capabilityClient.getCapability(CAPABILITY_WATCH_APP, any())
        } returns Tasks.forResult(
            TestCapabilityInfo(CAPABILITY_WATCH_APP, dummyNodes.toMutableSet())
        )

        connectionInterface.refreshNodesWithApp()

        // Verify nodes with app were checked
        verify { capabilityClient.getCapability(CAPABILITY_WATCH_APP, any()) }

        // Verify data updated
        assertThat(connectionInterface.nodesWithApp).containsExactlyElementsIn(dummyNodes)
    }

    @Test
    fun `refreshCapabilities calls appropriate functions and updates data`() {
        Capability.values().forEach {
            mockCapability(it, dummyNodes)
        }

        connectionInterface.refreshCapabilities()

        // Verify node capabilities were checked
        Capability.values().forEach {
            verify { capabilityClient.getCapability(it.name, any()) }
        }

        // Verify data was updated
        assertThat(connectionInterface.watchCapabilities.keys)
            .containsExactlyElementsIn(dummyNodes.map { it.id })
    }

    @Test
    fun `refreshCapabilities correctly updates data when a single node is present`() {
        val node = dummyNodes.first()

        // Check with all capabilities available for a node
        Capability.values().forEach {
            mockCapability(it, listOf(node))
        }
        connectionInterface.refreshCapabilities()
        (connectionInterface.watchCapabilities[node.id] ?: 0).let { capabilities ->
            Capability.values().forEach {
                val id = it.id
                assertThat((capabilities and id) == id).isTrue()
            }
        }

        // Check with sone capabilities available for a node
        val expectedCapabilities = listOf(Capability.SYNC_BATTERY, Capability.MANAGE_APPS)
        Capability.values().forEach {
            mockCapability(it, emptyList())
        }
        expectedCapabilities.forEach {
            mockCapability(it, listOf(node))
        }
        connectionInterface.refreshCapabilities()
        (connectionInterface.watchCapabilities[node.id] ?: 0).let { capabilities ->
            Capability.values().forEach {
                val id = it.id
                assertThat((capabilities and id) == id).isEqualTo(expectedCapabilities.contains(it))
            }
        }

        // Check with no capabilities available for a node
        Capability.values().forEach {
            mockCapability(it, emptyList())
        }
        connectionInterface.refreshCapabilities()
        (connectionInterface.watchCapabilities[node.id] ?: 0).let { capabilities ->
            Capability.values().forEach {
                val id = it.id
                assertThat((capabilities and id) == id).isFalse()
            }
        }
    }

    @Test
    fun `refreshCapabilities correctly updates data when multiple nodes are present`() {
        // Check with all capabilities available for a node
        Capability.values().forEach {
            mockCapability(it, dummyNodes)
        }
        connectionInterface.refreshCapabilities()
        dummyNodes.forEach { node ->
            (connectionInterface.watchCapabilities[node.id] ?: 0).let { capabilities ->
                Capability.values().forEach {
                    val id = it.id
                    assertThat((capabilities and id) == id).isTrue()
                }
            }
        }

        // Check with sone capabilities available for a node
        val expectedCapabilities = listOf(Capability.SYNC_BATTERY, Capability.MANAGE_APPS)
        Capability.values().forEach {
            mockCapability(it, emptyList())
        }
        expectedCapabilities.forEach {
            mockCapability(it, dummyNodes)
        }
        connectionInterface.refreshCapabilities()
        dummyNodes.forEach { node ->
            (connectionInterface.watchCapabilities[node.id] ?: 0).let { capabilities ->
                Capability.values().forEach {
                    val id = it.id
                    assertThat((capabilities and id) == id)
                        .isEqualTo(expectedCapabilities.contains(it))
                }
            }
        }

        // Check with no capabilities available for any nodes
        Capability.values().forEach {
            mockCapability(it, emptyList())
        }
        connectionInterface.refreshCapabilities()
        dummyNodes.forEach { node ->
            (connectionInterface.watchCapabilities[node.id] ?: 0).let { capabilities ->
                Capability.values().forEach {
                    val id = it.id
                    assertThat((capabilities and id) == id).isFalse()
                }
            }
        }
    }

    @Test
    fun `updatePreferenceOnWatch does nothing with invalid key`() {
        val key = "non-sync-key"
        connectionInterface.updatePreferenceOnWatch(dummyWatches.first(), key, 0)
        verify(inverse = true) { dataClient.putDataItem(any()) }
    }

    @Test
    fun `updatePreferenceOnWatch correctly syncs all bool prefs`() {
        SyncPreferences.BOOL_PREFS.forEach {
            connectionInterface.updatePreferenceOnWatch(dummyWatches.first(), it, false)
        }
        verify(exactly = SyncPreferences.BOOL_PREFS.count()) { dataClient.putDataItem(any()) }
    }

    @Test
    fun `updatePreferenceOnWatch correctly syncs all int prefs`() {
        SyncPreferences.INT_PREFS.forEach {
            connectionInterface.updatePreferenceOnWatch(dummyWatches.first(), it, 0)
        }
        verify(exactly = SyncPreferences.INT_PREFS.count()) { dataClient.putDataItem(any()) }
    }

    @Test
    fun `updatePreferenceOnWatch does nothing in invalid data type for key`() {
        SyncPreferences.BOOL_PREFS.forEach {
            connectionInterface.updatePreferenceOnWatch(dummyWatches.first(), it, 0)
        }
        SyncPreferences.INT_PREFS.forEach {
            connectionInterface.updatePreferenceOnWatch(dummyWatches.first(), it, false)
        }
        verify(inverse = true) { dataClient.putDataItem(any()) }
    }

    private fun mockCapability(capability: Capability, nodes: List<Node>) {
        every {
            capabilityClient.getCapability(capability.name, any())
        } returns Tasks.forResult(TestCapabilityInfo(capability.name, nodes.toMutableSet()))
    }
}
