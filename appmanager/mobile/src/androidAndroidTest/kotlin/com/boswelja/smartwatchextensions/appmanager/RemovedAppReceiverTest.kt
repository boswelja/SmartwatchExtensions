package com.boswelja.smartwatchextensions.appmanager

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.boswelja.watchconnection.common.message.ReceivedMessage
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule

class RemovedAppReceiverTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                factory<WatchAppRepository> { appRepository }
                factory<WatchAppIconRepository> { iconRepository }
            }
        )
    }

    private lateinit var context: Context

    private lateinit var appRepository: InMemoryWatchAppRepository
    private lateinit var iconRepository: InMemoryWatchAppIconRepository
    private lateinit var receiver: RemovedAppReceiver

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        appRepository = InMemoryWatchAppRepository()
        iconRepository = InMemoryWatchAppIconRepository()
        receiver = RemovedAppReceiver()
    }

    @Test
    fun onMessageReceived_removesAppsFromRepository() = runBlocking {
        val watchId = "some-id"
        val apps = createAppList(10)
        appRepository.appList.test {
            // Sanity test
            assert(awaitItem().isEmpty())

            // Populate repository
            appRepository.updateAll(apps.mapToWatchAppDetails(watchId))
            assert(awaitItem().isNotEmpty())

            // Make the call
            receiver.onMessageReceived(
                context,
                ReceivedMessage(
                    watchId,
                    RemovedAppsList,
                    RemovedApps(apps.apps.map { it.packageName })
                )
            )
            // All items should be removed, check the result
            assert(awaitItem().isEmpty())
        }
    }

    @Test
    fun onMessageReceived_removesIconsFromRepository() = runBlocking {
        val watchId = "some-id"
        val apps = createAppList(10)
        val dummyIcon = byteArrayOf(1, 2, 3)

        // Sanity test
        assert(iconRepository.iconMap.isEmpty())

        // Populate repository
        apps.apps.forEach { app ->
            iconRepository.storeIconFor(watchId, app.packageName, dummyIcon)
        }
        assert(iconRepository.iconMap.isNotEmpty())

        // Make the call
        receiver.onMessageReceived(
            context,
            ReceivedMessage(
                watchId,
                RemovedAppsList,
                RemovedApps(apps.apps.map { it.packageName })
            )
        )

        // Repository should be empty, check the result
        assert(iconRepository.iconMap.isEmpty())
    }
}
