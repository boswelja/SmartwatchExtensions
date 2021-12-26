package com.boswelja.smartwatchextensions.appmanager.ui

import app.cash.turbine.test
import com.boswelja.smartwatchextensions.appmanager.APP_SENDING_COMPLETE
import com.boswelja.smartwatchextensions.appmanager.APP_SENDING_START
import com.boswelja.smartwatchextensions.appmanager.AppVersion
import com.boswelja.smartwatchextensions.appmanager.AppVersions
import com.boswelja.smartwatchextensions.appmanager.CacheValidationSerializer
import com.boswelja.smartwatchextensions.appmanager.VALIDATE_CACHE
import com.boswelja.smartwatchextensions.appmanager.WatchApp
import com.boswelja.smartwatchextensions.appmanager.WatchAppIconRepository
import com.boswelja.smartwatchextensions.appmanager.WatchAppRepository
import com.boswelja.smartwatchextensions.appmanager.WatchAppVersion
import com.boswelja.smartwatchextensions.devicemanagement.SelectedWatchManager
import com.boswelja.watchconnection.common.Watch
import com.boswelja.watchconnection.common.discovery.ConnectionMode
import com.boswelja.watchconnection.common.message.ReceivedMessage
import com.boswelja.watchconnection.core.discovery.DiscoveryClient
import com.boswelja.watchconnection.core.message.MessageClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppManagerViewModelTest {

    private lateinit var appRepository: WatchAppRepository
    private lateinit var messageClient: MessageClient
    private lateinit var discoveryClient: DiscoveryClient
    private lateinit var selectedWatchManager: SelectedWatchManager
    private lateinit var appIconRepository: WatchAppIconRepository

    private val selectedWatch = MutableStateFlow<Watch?>(null)
    private val incomingMessages = MutableStateFlow<ReceivedMessage<ByteArray?>>(
        ReceivedMessage("", "", null)
    )

    private lateinit var viewModel: AppManagerViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        // We use unconfined here so Turbine will actually work
        Dispatchers.setMain(UnconfinedTestDispatcher())

        appRepository = mockk()
        messageClient = mockk()
        discoveryClient = mockk()
        selectedWatchManager = mockk()
        appIconRepository = mockk()

        selectedWatch.value = null
        incomingMessages.value = ReceivedMessage("", "", null)
        every { selectedWatchManager.selectedWatch } returns selectedWatch
        every { messageClient.incomingMessages() } returns incomingMessages

        // Mocks to stub validateCache
        every { appRepository.getAppVersionsFor(any()) } returns flowOf(emptyList())
        coEvery { messageClient.sendMessage(any(), any()) } returns true

        viewModel = createViewModel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun validateCache_isCalledWhenSelectedWatchChanges() {
        val iterations = 3
        (0 until iterations).forEach {
            val watchUid = Watch.createUid("platform", "$it")
            val watch = Watch(watchUid, "Watch $it")
            selectedWatch.value = watch

            verify(timeout = 1000) { appRepository.getAppVersionsFor(watchUid) }
        }
    }

    @Test
    fun validateCache_sendsCacheState() = runTest {
        val watchUid = Watch.createUid("platform", "id")
        val watch = Watch(watchUid, "Watch")
        val versions = listOf(
            WatchAppVersion("com.my.package", 1),
            WatchAppVersion("org.package2", 2),
            WatchAppVersion("com.package.three", 100)
        )
        val appVersions = versions.map { AppVersion(it.packageName, it.versionCode) }
        val versionBytes = CacheValidationSerializer.serialize(AppVersions(appVersions))

        every { appRepository.getAppVersionsFor(any()) } returns flowOf(versions)
        selectedWatch.value = watch

        coVerify(timeout = 1000) {
            messageClient.sendMessage(
                watchUid,
                match {
                    it.path == VALIDATE_CACHE && it.data.contentEquals(versionBytes)
                }
            )
        }
    }

    @Test
    fun isUpdatingCache_updatesCorrectly(): Unit = runBlocking {
        viewModel.isUpdatingCache.test {
            // Check initial value
            assertFalse(awaitItem())

            // Simulate app sending start
            incomingMessages.emit(
                ReceivedMessage(
                    "uid",
                    APP_SENDING_START,
                    null
                )
            )
            assertTrue(awaitItem())

            // Simulate app sending complete
            incomingMessages.emit(
                ReceivedMessage(
                    "uid",
                    APP_SENDING_COMPLETE,
                    null
                )
            )
            assertFalse(awaitItem())
        }
    }

    @Test
    fun isUpdatingCache_handlesIncorrectMessage(): Unit = runBlocking {
        viewModel.isUpdatingCache.test {
            // Check initial value
            assertFalse(awaitItem())

            // Simulate incorrect message
            incomingMessages.emit(
                ReceivedMessage(
                    "uid",
                    VALIDATE_CACHE,
                    null
                )
            )
            expectNoEvents()
        }
    }

    @Test
    fun isWatchConnected_updatesCorrectly(): Unit = runBlocking {
        val connectionModeFlow = MutableStateFlow(ConnectionMode.Disconnected)
        every { discoveryClient.connectionModeFor(any<String>()) } returns connectionModeFlow

        viewModel.isWatchConnected.test {
            // Check initial value
            assertTrue(awaitItem())

            // Value is updated when selected watch changes
            connectionModeFlow.emit(ConnectionMode.Disconnected)
            selectedWatch.emit(
                Watch("Watch", "id", "platform")
            )
            awaitEvent()

            // Value is updated when connection mode changes
            connectionModeFlow.emit(ConnectionMode.Bluetooth)
            awaitEvent()

            // Emits false when disconnected
            connectionModeFlow.emit(ConnectionMode.Disconnected)
            assertFalse(awaitItem())

            // Emits true when connected via Internet or Bluetooth
            connectionModeFlow.emit(ConnectionMode.Internet)
            assertTrue(awaitItem())
            connectionModeFlow.emit(ConnectionMode.Bluetooth)
            expectNoEvents()
        }
    }

    @Test
    fun userApps_containsOnlyUserApps(): Unit = runBlocking {
        val watchApps = createWatchApps(100)
        val watchUid = Watch.createUid("platform", "id")
        val watch = Watch(watchUid, "Watch")

        every { appRepository.getAppsFor(watchUid) } returns flowOf(watchApps)
        coEvery { appIconRepository.retrieveIconFor(any(), any()) } returns null
        selectedWatch.value = watch

        viewModel.userApps.test {
            assertTrue { awaitItem().all { it.isEnabled && !it.isSystemApp } }
        }
    }

    @Test
    fun systemApps_containsOnlySystemApps(): Unit = runBlocking {
        val watchApps = createWatchApps(100)
        val watchUid = Watch.createUid("platform", "id")
        val watch = Watch(watchUid, "Watch")

        every { appRepository.getAppsFor(watchUid) } returns flowOf(watchApps)
        coEvery { appIconRepository.retrieveIconFor(any(), any()) } returns null
        selectedWatch.value = watch

        viewModel.systemApps.test {
            assertTrue { awaitItem().all { it.isEnabled && it.isSystemApp } }
        }
    }

    @Test
    fun disabledApps_containsOnlyDisabledApps(): Unit = runBlocking {
        val watchApps = createWatchApps(100)
        val watchUid = Watch.createUid("platform", "id")
        val watch = Watch(watchUid, "Watch")

        every { appRepository.getAppsFor(watchUid) } returns flowOf(watchApps)
        coEvery { appIconRepository.retrieveIconFor(any(), any()) } returns null
        selectedWatch.value = watch

        viewModel.disabledApps.test {
            assertTrue { awaitItem().all { !it.isEnabled } }
        }
    }

    private fun createViewModel(): AppManagerViewModel =
        AppManagerViewModel(
            appRepository,
            messageClient,
            discoveryClient,
            selectedWatchManager,
            appIconRepository
        )

    private fun createWatchApps(count: Int): List<WatchApp> {
        return (0 until count).map {
            val isSystemApp = Random.nextFloat() < 0.3
            val isEnabled = Random.nextFloat() > 0.2
            WatchApp(
                packageName = "com.package$it",
                label = "Package $it",
                versionName = "v$it",
                isSystemApp = isSystemApp,
                isEnabled = isEnabled
            )
        }
    }
}
