package com.boswelja.devicemanager.watchmanager.connection

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.TestCapabilityInfo
import com.boswelja.devicemanager.TestNode
import com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP
import com.boswelja.devicemanager.getOrAwaitValue
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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class WearOSConnectionInterfaceTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

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

    @After
    fun tearDown() {
        connectionInterface.dispose()
    }

    @Test
    fun `availableWatches contains only the watches that have the app and are connected`() {
        connectionInterface = WearOSConnectionInterface(
            capabilityClient,
            nodeClient,
            messageClient,
            dataClient
        )
        var expectedWatches = dummyWatches
        // Test with matching sets
        connectionInterface.connectedNodes.value = dummyNodes.toList()
        connectionInterface.nodesWithApp.value = dummyNodes
        assertThat(connectionInterface.availableWatches.getOrAwaitValue())
            .containsExactlyElementsIn(expectedWatches)

        // Test with more connected nodes than capable nodes
        val nodesWithApp = dummyNodes.drop(1).toSet()
        expectedWatches = nodesWithApp.map(transformNodeToWatch)
        connectionInterface.connectedNodes.value = dummyNodes.toList()
        connectionInterface.nodesWithApp.value = nodesWithApp
        assertThat(connectionInterface.availableWatches.getOrAwaitValue())
            .containsExactlyElementsIn(expectedWatches)

        // Test with more capable nodes than connected nodes
        val connectedNodes = dummyNodes.drop(1)
        expectedWatches = connectedNodes.map(transformNodeToWatch)
        connectionInterface.connectedNodes.value = connectedNodes
        connectionInterface.nodesWithApp.value = dummyNodes
        assertThat(connectionInterface.availableWatches.getOrAwaitValue())
            .containsExactlyElementsIn(expectedWatches)

        // Test with no nodes
        connectionInterface.connectedNodes.value = emptyList()
        connectionInterface.nodesWithApp.value = emptySet()
        assertThat(connectionInterface.availableWatches.getOrAwaitValue()).isEmpty()
    }

    @Test
    fun `sendMessage passes the request to MessageClient`() {
        connectionInterface = WearOSConnectionInterface(
            capabilityClient,
            nodeClient,
            messageClient,
            dataClient
        )
        val node = dummyNodes.first()
        val dummyWatch = transformNodeToWatch(node)
        val path = "/message-path"
        val data = ByteArray(8)
        connectionInterface.sendMessage(dummyWatch.id, path, data)
        verify(exactly = 1) { messageClient.sendMessage(dummyWatch.id, path, data) }
    }

    @Test
    fun `getWatchStatus returns the correct status`() {
        connectionInterface = WearOSConnectionInterface(
            capabilityClient,
            nodeClient,
            messageClient,
            dataClient
        )
        val dummyNode = dummyNodes.first()
        val dummyWatch = transformNodeToWatch(dummyNode)

        connectionInterface.nodesWithApp.value = setOf(dummyNode)
        connectionInterface.connectedNodes.value = listOf(dummyNode)
        assertThat(connectionInterface.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(Watch.Status.CONNECTED)

        connectionInterface.nodesWithApp.value = setOf(dummyNode)
        connectionInterface.connectedNodes.value = listOf(dummyNode)
        assertThat(connectionInterface.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(Watch.Status.NOT_REGISTERED)

        connectionInterface.nodesWithApp.value = emptySet()
        connectionInterface.connectedNodes.value = listOf(dummyNode)
        assertThat(connectionInterface.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(Watch.Status.MISSING_APP)

        connectionInterface.nodesWithApp.value = emptySet()
        connectionInterface.connectedNodes.value = listOf(dummyNode)
        assertThat(connectionInterface.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(Watch.Status.MISSING_APP)

        connectionInterface.nodesWithApp.value = setOf(dummyNode)
        connectionInterface.connectedNodes.value = emptyList()
        assertThat(connectionInterface.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(Watch.Status.DISCONNECTED)

        connectionInterface.nodesWithApp.value = setOf(dummyNode)
        connectionInterface.connectedNodes.value = emptyList()
        assertThat(connectionInterface.getWatchStatus(dummyWatch, false))
            .isEquivalentAccordingToCompareTo(Watch.Status.ERROR)

        connectionInterface.nodesWithApp.value = emptySet()
        connectionInterface.connectedNodes.value = emptyList()
        assertThat(connectionInterface.getWatchStatus(dummyWatch, true))
            .isEquivalentAccordingToCompareTo(Watch.Status.ERROR)

        connectionInterface.nodesWithApp.value = emptySet()
        connectionInterface.connectedNodes.value = emptyList()
        assertThat(connectionInterface.getWatchStatus(dummyWatch, false))
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
        connectionInterface = WearOSConnectionInterface(
            capabilityClient,
            nodeClient,
            messageClient,
            dataClient
        )

        task.continueWith {
            verify(exactly = 1) { capabilityClient.getCapability(CAPABILITY_WATCH_APP, any()) }
            assertThat(connectionInterface.connectedNodes.value)
                .containsExactlyElementsIn(dummyNodes)
        }
    }
}
