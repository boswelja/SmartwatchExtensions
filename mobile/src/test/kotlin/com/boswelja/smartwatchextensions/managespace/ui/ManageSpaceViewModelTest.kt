package com.boswelja.smartwatchextensions.managespace.ui

import android.app.Application
import android.os.Build
import android.os.Looper.getMainLooper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.analytics.Analytics
import com.boswelja.smartwatchextensions.appsettings.Settings
import com.boswelja.smartwatchextensions.managespace.ui.ManageSpaceViewModel.Companion.MAX_PROGRESS
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import com.boswelja.watchconnection.core.Watch
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isLessThanOrEqualTo

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class ManageSpaceViewModelTest {

    @get:Rule val taskExecutorRule = InstantTaskExecutorRule()

    private val dummyWatch1 = Watch("Watch 1", "id1", "")
    private val dummyWatch2 = Watch("Watch 2", "id2", "")
    private val dummyWatch3 = Watch("Watch 3", "id3", "")
    private val dummyWatches = listOf(dummyWatch1, dummyWatch2, dummyWatch3)
    private val appSettings = MutableStateFlow(Settings())

    private val coroutineDispatcher = TestCoroutineDispatcher()

    private val registeredWatches = MutableStateFlow<List<Watch>>(emptyList())

    private lateinit var viewModel: ManageSpaceViewModel

    @RelaxedMockK lateinit var dataStore: DataStore<Settings>
    @RelaxedMockK lateinit var analytics: Analytics
    @RelaxedMockK lateinit var watchManager: WatchManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { watchManager.registeredWatches } returns registeredWatches
        every { dataStore.data } returns appSettings
        viewModel = ManageSpaceViewModel(
            ApplicationProvider.getApplicationContext(),
            analytics,
            watchManager,
            dataStore,
            coroutineDispatcher
        )
    }

    @Test
    fun `resetAnalytics resets analytics`() {
        viewModel.resetAnalytics { }
        shadowOf(getMainLooper()).idle()
        verify(exactly = 1) { analytics.logStorageManagerAction(any()) }
        verify(exactly = 1) { analytics.resetAnalytics() }
    }

    @Test
    fun `resetAnalytics calls onComplete`() {
        val onComplete: (Boolean) -> Unit = mockk(relaxed = true)
        viewModel.resetAnalytics(onComplete)
        shadowOf(getMainLooper()).idle()
        verify(exactly = 1) { onComplete(any()) }
    }

    @Test
    fun `clearCache calls onComplete`() {
        val onComplete: (Boolean) -> Unit = mockk(relaxed = true)
        viewModel.clearCache(
            { },
            onComplete
        )
        shadowOf(getMainLooper()).idle()
        verify(exactly = 1) { onComplete(any()) }
    }

    @Test
    fun `clearCache clears cache`() {
        val cacheDir = ApplicationProvider.getApplicationContext<Application>().cacheDir
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val testFile = File(cacheDir, "test")
        testFile.createNewFile()

        viewModel.clearCache({ }, { })
        shadowOf(getMainLooper()).idle()
        expectThat(testFile.exists()).isFalse()
    }

    @Test
    fun `clearCache onProgressChanged never exceeds MAX_PROGRESS`() {
        val cacheDir = ApplicationProvider.getApplicationContext<Application>().cacheDir
        var currentProgress = 0
        val onProgressChanged: (Int) -> Unit = {
            currentProgress = it
        }

        // Test with multiple cache files
        if (!cacheDir.exists()) cacheDir.mkdirs()
        (1..10).forEach {
            File(cacheDir, it.toString()).createNewFile()
        }
        viewModel.clearCache(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        expectThat(currentProgress).isLessThanOrEqualTo(MAX_PROGRESS.toInt())

        // Test with single cache file
        if (!cacheDir.exists()) cacheDir.mkdirs()
        File(cacheDir, "test").createNewFile()
        viewModel.clearCache(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        expectThat(currentProgress).isLessThanOrEqualTo(MAX_PROGRESS.toInt())

        // Test with no cache files
        cacheDir.listFiles()?.forEach { it.deleteRecursively() }
        viewModel.clearCache(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        expectThat(currentProgress).isLessThanOrEqualTo(MAX_PROGRESS.toInt())
    }

    @Test
    fun `resetExtensionSettings calls onComplete`() {
        val onComplete: (Boolean) -> Unit = mockk(relaxed = true)
        viewModel.resetExtensionSettings(
            { },
            onComplete
        )
        shadowOf(getMainLooper()).idle()
        verify(exactly = 1) { onComplete(any()) }
    }

    @Test
    fun `resetExtensionSettings resets extension settings`() {
        registeredWatches.value = dummyWatches
        viewModel.resetExtensionSettings({ }, { })
        shadowOf(getMainLooper()).idle()

        verify(exactly = 1) { analytics.logStorageManagerAction(any()) }
        dummyWatches.forEach {
            coVerify(exactly = 1) {
                watchManager.resetWatchPreferences(
                    ApplicationProvider.getApplicationContext<Application>(), it
                )
            }
        }
    }

    @Test
    fun `resetExtensionSettings onProgressChanged never exceeds MAX_PROGRESS`() {
        var currentProgress = 0
        val onProgressChanged: (Int) -> Unit = {
            currentProgress = it
        }

        // Test with multiple registeredWatches
        registeredWatches.value = dummyWatches
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        expectThat(currentProgress).isLessThanOrEqualTo(MAX_PROGRESS.toInt())

        // Test with single registered watch
        registeredWatches.value = listOf(dummyWatch1)
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        expectThat(currentProgress).isLessThanOrEqualTo(MAX_PROGRESS.toInt())

        // Test with no registered watches
        registeredWatches.value = emptyList()
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        expectThat(currentProgress).isLessThanOrEqualTo(MAX_PROGRESS.toInt())
    }

    @Test
    fun `resetAppSettings calls onComplete`() {
        val onComplete: (Boolean) -> Unit = mockk(relaxed = true)
        viewModel.resetAppSettings(
            onComplete
        )
        shadowOf(getMainLooper()).idle()
        verify(exactly = 1) { onComplete(any()) }
    }

    @Test
    fun `resetAppSettings resets app settings`(): Unit = runBlocking {
        viewModel.resetAppSettings { }
        shadowOf(getMainLooper()).idle()
        coVerify {
            dataStore.updateData(any())
        }
    }

    @Test
    fun `resetApp calls onComplete`() {
        val onComplete: (Boolean) -> Unit = mockk(relaxed = true)
        viewModel.resetApp(
            { },
            onComplete
        )
        shadowOf(getMainLooper()).idle()
        verify(exactly = 1) { onComplete(any()) }
    }

    @Test
    fun `resetApp resets the app`() {
        registeredWatches.value = dummyWatches

        viewModel.resetApp({ }, { })
        shadowOf(getMainLooper()).idle()

        verify(exactly = 1) { analytics.logStorageManagerAction(any()) }
        verify(exactly = 1) { analytics.resetAnalytics() }
        dummyWatches.forEach {
            coVerify(exactly = 1) {
                watchManager.forgetWatch(
                    ApplicationProvider.getApplicationContext<Application>(), it
                )
            }
        }
    }

    @Test
    fun `resetApp onProgressChanged never exceeds MAX_PROGRESS`() {
        var currentProgress = 0
        val onProgressChanged: (Int) -> Unit = {
            currentProgress = it
        }

        // Test with registered watches
        registeredWatches.value = dummyWatches
        viewModel.resetApp(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        expectThat(currentProgress).isLessThanOrEqualTo(MAX_PROGRESS.toInt())

        // Test with registered watch
        registeredWatches.value = listOf(dummyWatch1)
        viewModel.resetApp(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        expectThat(currentProgress).isLessThanOrEqualTo(MAX_PROGRESS.toInt())

        // Test with no registered watches
        registeredWatches.value = emptyList()
        viewModel.resetApp(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        expectThat(currentProgress).isLessThanOrEqualTo(MAX_PROGRESS.toInt())
    }
}
