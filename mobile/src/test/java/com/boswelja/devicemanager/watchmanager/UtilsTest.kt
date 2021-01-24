package com.boswelja.devicemanager.watchmanager

import com.boswelja.devicemanager.watchmanager.Utils.getWatchStatus
import com.boswelja.devicemanager.watchmanager.communication.WatchStatus
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.wearable.Node
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class UtilsTest {

    private val dummyWatch = Watch("an-id-1234", "Watch 1")
    private val dummyWatchNode =
        object : Node {
            override fun getDisplayName(): String = dummyWatch.name
            override fun getId(): String = dummyWatch.id
            override fun isNearby(): Boolean = true
        }

    @RelaxedMockK
    lateinit var database: WatchDatabase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { database.isOpen } returns true
    }

    @Test
    fun `getWatchStatus returns the correct status when the watch is registered`() = runBlocking {
        // Make sure isRegistered will evaluate to true
        every { database.watchDao().get(dummyWatch.id) } returns dummyWatch

        var result = getWatchStatus(dummyWatch.id, database)
        assertThat(result).isEqualTo(WatchStatus.DISCONNECTED)

        result = getWatchStatus(dummyWatch.id, database, setOf(dummyWatchNode))
        assertThat(result).isEqualTo(WatchStatus.DISCONNECTED)

        result = getWatchStatus(dummyWatch.id, database, connectedNodes = listOf(dummyWatchNode))
        assertThat(result).isEqualTo(WatchStatus.ERROR)

        result = getWatchStatus(dummyWatch.id, database, setOf(), listOf())
        assertThat(result).isEqualTo(WatchStatus.DISCONNECTED)

        result =
            getWatchStatus(
                dummyWatch.id, database, setOf(dummyWatchNode), listOf(dummyWatchNode)
            )
        assertThat(result).isEqualTo(WatchStatus.CONNECTED)
    }

    @Test
    fun `getWatchStatus returns the correct status when the watch is unregistered`() = runBlocking {
        // Make sure isRegistered will evaluate to false
        every { database.watchDao().get(dummyWatch.id) } returns null

        var result =
            getWatchStatus(
                dummyWatch.id, database, setOf(dummyWatchNode), listOf(dummyWatchNode)
            )
        assertThat(result).isEqualTo(WatchStatus.NOT_REGISTERED)

        result = getWatchStatus(dummyWatch.id, database, setOf(dummyWatchNode), listOf())
        assertThat(result).isEqualTo(WatchStatus.NOT_REGISTERED)

        result = getWatchStatus(dummyWatch.id, database, setOf(dummyWatchNode))
        assertThat(result).isEqualTo(WatchStatus.NOT_REGISTERED)

        result = getWatchStatus(dummyWatch.id, database, setOf(), listOf(dummyWatchNode))
        assertThat(result).isEqualTo(WatchStatus.MISSING_APP)

        result = getWatchStatus(dummyWatch.id, database, connectedNodes = listOf(dummyWatchNode))
        assertThat(result).isEqualTo(WatchStatus.MISSING_APP)

        result = getWatchStatus(dummyWatch.id, database, setOf(), listOf())
        assertThat(result).isEqualTo(WatchStatus.MISSING_APP)

        result = getWatchStatus(dummyWatch.id, database)
        assertThat(result).isEqualTo(WatchStatus.MISSING_APP)
    }
}
