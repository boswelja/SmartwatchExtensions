package com.boswelja.smartwatchextensions.appmanager

import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.boswelja.watchconnection.common.message.ReceivedMessage
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule

class AddedOrUpdatedAppReceiverTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                factory<WatchAppRepository> { repository }
            }
        )
    }

    private lateinit var repository: InMemoryWatchAppRepository

    @Before
    fun setUp() {
        repository = InMemoryWatchAppRepository()
    }

    @Test
    fun onMessageReceived_storesAppsInRepository() = runBlocking {
        // Set up test
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val receiver = AddedOrUpdatedAppReceiver()
        val watchId = "watch-id"
        val appCount = 100
        val appList = createAppList(appCount)

        // Check the repository
        repository.appList.test {
            // Sanity check
            assert(awaitItem().isEmpty())

            // Make the call
            receiver.onMessageReceived(
                context,
                ReceivedMessage(watchId, AddedAppsList, appList)
            )

            // Check the result
            val items = awaitItem()
            assert(items.count() == appCount)
            assert(items.all { it.watchId == watchId })
        }
    }
}
