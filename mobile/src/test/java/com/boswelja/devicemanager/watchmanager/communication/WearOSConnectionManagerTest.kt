package com.boswelja.devicemanager.watchmanager.communication

import com.boswelja.devicemanager.TestNode
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class WearOSConnectionManagerTest {

    private val dummyNodes = setOf<Node>(
        TestNode("id1", "Watch 1"),
        TestNode("id2", "Watch 2"),
        TestNode("id3", "Watch 3")
    )

    @RelaxedMockK private lateinit var capabilityClient: CapabilityClient
    @RelaxedMockK private lateinit var nodeClient: NodeClient
    @RelaxedMockK private lateinit var messageClient: MessageClient

    private lateinit var connectionManager: WearOSConnectionManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        connectionManager = WearOSConnectionManager(capabilityClient, nodeClient, messageClient)
    }

    @After
    fun tearDown() {
        connectionManager.dispose()
    }

    @Test
    fun `getAvailableWatches returns only the watches that have the app and are connected`() {
        var expectedWatches = dummyNodes.map { Watch(it) }
        // Test with matching sets
        connectionManager.connectedNodes = dummyNodes.toList()
        connectionManager.nodesWithApp = dummyNodes
        assertThat(connectionManager.getAvailableWatches())
            .containsExactlyElementsIn(expectedWatches)

        // Test with more connected nodes than capable nodes
        val nodesWithApp = dummyNodes.drop(1).toSet()
        expectedWatches = nodesWithApp.map { Watch(it) }
        connectionManager.connectedNodes = dummyNodes.toList()
        connectionManager.nodesWithApp = nodesWithApp
        assertThat(connectionManager.getAvailableWatches())
            .containsExactlyElementsIn(expectedWatches)

        // Test with more capable nodes than connected nodes
        val connectedNodes = dummyNodes.drop(1)
        expectedWatches = connectedNodes.map { Watch(it) }
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
        val dummyWatch = Watch(dummyNodes.first())
        val path = "/message-path"
        val data = ByteArray(8)
        connectionManager.sendMessage(dummyWatch.id, path, data)
        verify(exactly = 1) { messageClient.sendMessage(dummyWatch.id, path, data) }
    }

    @Test
    fun `getWatchStatus returns the correct status`() {
        val dummyNode = dummyNodes.first()
        val dummyWatch = Watch(dummyNode)

        connectionManager.nodesWithApp = setOf(dummyNode)
        connectionManager.connectedNodes = listOf(dummyNode)
        assertThat(connectionManager.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(WatchStatus.CONNECTED)

        connectionManager.nodesWithApp = setOf(dummyNode)
        connectionManager.connectedNodes = listOf(dummyNode)
        assertThat(connectionManager.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(WatchStatus.NOT_REGISTERED)

        connectionManager.nodesWithApp = emptySet()
        connectionManager.connectedNodes = listOf(dummyNode)
        assertThat(connectionManager.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(WatchStatus.MISSING_APP)

        connectionManager.nodesWithApp = emptySet()
        connectionManager.connectedNodes = listOf(dummyNode)
        assertThat(connectionManager.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(WatchStatus.MISSING_APP)

        connectionManager.nodesWithApp = setOf(dummyNode)
        connectionManager.connectedNodes = emptyList()
        assertThat(connectionManager.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(WatchStatus.DISCONNECTED)

        connectionManager.nodesWithApp = setOf(dummyNode)
        connectionManager.connectedNodes = emptyList()
        assertThat(connectionManager.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(WatchStatus.ERROR)

        connectionManager.nodesWithApp = emptySet()
        connectionManager.connectedNodes = emptyList()
        assertThat(connectionManager.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(WatchStatus.ERROR)

        connectionManager.nodesWithApp = emptySet()
        connectionManager.connectedNodes = emptyList()
        assertThat(connectionManager.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(WatchStatus.ERROR)
    }
}
