package com.boswelja.devicemanager.watchmanager.communication

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.TestCapabilityInfo
import com.boswelja.devicemanager.TestNode
import com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class WearOSConnectionManagerTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val transformNodeToWatch: (node: Node) -> Watch = {
        Watch(it.id, it.displayName, Watch.Platform.WEAR_OS)
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

    private lateinit var connectionManager: WearOSConnectionManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        connectionManager.dispose()
    }

    @Test
    fun `getAvailableWatches returns only the watches that have the app and are connected`() {
        connectionManager = WearOSConnectionManager(capabilityClient, nodeClient, messageClient)
        var expectedWatches = dummyWatches
        // Test with matching sets
        connectionManager.connectedNodes = dummyNodes.toList()
        connectionManager.nodesWithApp = dummyNodes
        assertThat(connectionManager.getAvailableWatches())
            .containsExactlyElementsIn(expectedWatches)

        // Test with more connected nodes than capable nodes
        val nodesWithApp = dummyNodes.drop(1).toSet()
        expectedWatches = nodesWithApp.map(transformNodeToWatch)
        connectionManager.connectedNodes = dummyNodes.toList()
        connectionManager.nodesWithApp = nodesWithApp
        assertThat(connectionManager.getAvailableWatches())
            .containsExactlyElementsIn(expectedWatches)

        // Test with more capable nodes than connected nodes
        val connectedNodes = dummyNodes.drop(1)
        expectedWatches = connectedNodes.map(transformNodeToWatch)
        connectionManager.connectedNodes = connectedNodes
        connectionManager.nodesWithApp = dummyNodes
        assertThat(connectionManager.getAvailableWatches())
            .containsExactlyElementsIn(expectedWatches)

        // Test with no nodes
        connectionManager.connectedNodes = emptyList()
        connectionManager.nodesWithApp = emptySet()
        assertThat(connectionManager.getAvailableWatches()).isEmpty()
    }

    @Test
    fun `sendMessage passes the request to MessageClient`() {
        connectionManager = WearOSConnectionManager(capabilityClient, nodeClient, messageClient)
        val node = dummyNodes.first()
        val dummyWatch = transformNodeToWatch(node)
        val path = "/message-path"
        val data = ByteArray(8)
        connectionManager.sendMessage(dummyWatch.id, path, data)
        verify(exactly = 1) { messageClient.sendMessage(dummyWatch.id, path, data) }
    }

    @Test
    fun `getWatchStatus returns the correct status`() {
        connectionManager = WearOSConnectionManager(capabilityClient, nodeClient, messageClient)
        val dummyNode = dummyNodes.first()
        val dummyWatch = transformNodeToWatch(dummyNode)

        connectionManager.nodesWithApp = setOf(dummyNode)
        connectionManager.connectedNodes = listOf(dummyNode)
        assertThat(connectionManager.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(Watch.Status.CONNECTED)

        connectionManager.nodesWithApp = setOf(dummyNode)
        connectionManager.connectedNodes = listOf(dummyNode)
        assertThat(connectionManager.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(Watch.Status.NOT_REGISTERED)

        connectionManager.nodesWithApp = emptySet()
        connectionManager.connectedNodes = listOf(dummyNode)
        assertThat(connectionManager.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(Watch.Status.MISSING_APP)

        connectionManager.nodesWithApp = emptySet()
        connectionManager.connectedNodes = listOf(dummyNode)
        assertThat(connectionManager.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(Watch.Status.MISSING_APP)

        connectionManager.nodesWithApp = setOf(dummyNode)
        connectionManager.connectedNodes = emptyList()
        assertThat(connectionManager.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(Watch.Status.DISCONNECTED)

        connectionManager.nodesWithApp = setOf(dummyNode)
        connectionManager.connectedNodes = emptyList()
        assertThat(connectionManager.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(Watch.Status.ERROR)

        connectionManager.nodesWithApp = emptySet()
        connectionManager.connectedNodes = emptyList()
        assertThat(connectionManager.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(Watch.Status.ERROR)

        connectionManager.nodesWithApp = emptySet()
        connectionManager.connectedNodes = emptyList()
        assertThat(connectionManager.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(Watch.Status.ERROR)
    }

    @Test
    fun `capableNodes is correctly updated on init`() {
        val task = Tasks.forResult(
            TestCapabilityInfo(CAPABILITY_WATCH_APP, dummyNodes.toMutableSet()) as CapabilityInfo
        )
        every {
            capabilityClient.getCapability(CAPABILITY_WATCH_APP, any())
        } returns task
        connectionManager = WearOSConnectionManager(capabilityClient, nodeClient, messageClient)

        task.continueWith {
            verify(exactly = 1) { capabilityClient.getCapability(CAPABILITY_WATCH_APP, any()) }
            assertThat(connectionManager.connectedNodes).containsExactlyElementsIn(dummyNodes)
        }
    }
}
