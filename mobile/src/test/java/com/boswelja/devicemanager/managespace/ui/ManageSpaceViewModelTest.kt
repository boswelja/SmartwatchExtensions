package com.boswelja.devicemanager.managespace.ui

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import android.os.Looper.getMainLooper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.managespace.ui.ManageSpaceViewModel.Companion.MAX_PROGRESS
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class ManageSpaceViewModelTest {

    @get:Rule val taskExecutorRule = InstantTaskExecutorRule()

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1", "")
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2", "")
    private val dummyWatch3 = Watch("an-id-3456", "Watch 3", "")
    private val dummyWatches = listOf(dummyWatch1, dummyWatch2, dummyWatch3)

    private val coroutineDispatcher = TestCoroutineDispatcher()

    private val registeredWatches = MutableLiveData<List<Watch>>(emptyList())

    private lateinit var viewModel: ManageSpaceViewModel
    private lateinit var sharedPreferences: SharedPreferences

    @RelaxedMockK lateinit var analytics: Analytics
    @RelaxedMockK lateinit var watchManager: WatchManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        every { watchManager.registeredWatches } returns registeredWatches
        viewModel = ManageSpaceViewModel(
            ApplicationProvider.getApplicationContext(),
            analytics,
            watchManager,
            sharedPreferences,
            coroutineDispatcher
        )
    }

    @After
    fun tearDown() {
        sharedPreferences.edit(commit = true) { clear() }
    }

    @Test
    fun `registeredWatches is observed for the lifecycle of the view model`() {
        verify { watchManager.registeredWatches }
        assertThat(registeredWatches.hasActiveObservers()).isTrue()
        viewModel.onCleared()
        verify { watchManager.registeredWatches }
        assertThat(registeredWatches.hasActiveObservers()).isFalse()
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
        assertThat(testFile.exists()).isFalse()
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
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with single cache file
        if (!cacheDir.exists()) cacheDir.mkdirs()
        File(cacheDir, "test").createNewFile()
        viewModel.clearCache(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with no cache files
        cacheDir.listFiles()?.forEach { it.deleteRecursively() }
        viewModel.clearCache(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())
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
        // Populate preferences with some preferences
        val nonExtensionKey = "dummy-key"
        sharedPreferences.edit(commit = true) {
            putString(nonExtensionKey, nonExtensionKey)
            SyncPreferences.ALL_PREFS.forEach {
                putInt(it, 1)
            }
        }

        viewModel.resetExtensionSettings({ }, { })
        shadowOf(getMainLooper()).idle()

        verify(exactly = 1) { analytics.logStorageManagerAction(any()) }
        dummyWatches.forEach {
            coVerify(exactly = 1) {
                watchManager.resetWatchPreferences(ApplicationProvider.getApplicationContext(), it)
            }
        }
        assertThat(sharedPreferences.contains(nonExtensionKey)).isTrue()
        SyncPreferences.ALL_PREFS.forEach {
            assertThat(sharedPreferences.contains(it)).isFalse()
        }
    }

    @Test
    fun `resetExtensionSettings onProgressChanged never exceeds MAX_PROGRESS`() {
        val nonExtensionKey = "dummy-key"
        var currentProgress = 0
        val onProgressChanged: (Int) -> Unit = {
            currentProgress = it
        }

        // Test with registeredWatches and full sharedPreferences
        registeredWatches.value = dummyWatches
        sharedPreferences.edit(commit = true) {
            putString(nonExtensionKey, nonExtensionKey)
            SyncPreferences.ALL_PREFS.forEach {
                putInt(it, 1)
            }
        }
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with single registered watch and full sharedPreferences
        registeredWatches.value = listOf(dummyWatch1)
        sharedPreferences.edit(commit = true) {
            putString(nonExtensionKey, nonExtensionKey)
            SyncPreferences.ALL_PREFS.forEach {
                putInt(it, 1)
            }
        }
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with no registered watch and full sharedPreferences
        registeredWatches.value = emptyList()
        sharedPreferences.edit(commit = true) {
            putString(nonExtensionKey, nonExtensionKey)
            SyncPreferences.ALL_PREFS.forEach {
                putInt(it, 1)
            }
        }
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with registeredWatches and only extension sharedPreferences
        registeredWatches.value = dummyWatches
        sharedPreferences.edit(commit = true) {
            remove(nonExtensionKey)
            SyncPreferences.ALL_PREFS.forEach {
                putInt(it, 1)
            }
        }
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with registeredWatches and only app sharedPreferences
        registeredWatches.value = dummyWatches
        sharedPreferences.edit(commit = true) {
            clear()
            putString(nonExtensionKey, nonExtensionKey)
        }
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with registeredWatches and no sharedPreferences
        registeredWatches.value = dummyWatches
        sharedPreferences.edit(commit = true) {
            clear()
        }
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with single registered watch and extension sharedPreferences
        registeredWatches.value = listOf(dummyWatch1)
        sharedPreferences.edit(commit = true) {
            remove(nonExtensionKey)
            SyncPreferences.ALL_PREFS.forEach {
                putInt(it, 1)
            }
        }
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with single registered watch and only app sharedPreferences
        registeredWatches.value = listOf(dummyWatch1)
        sharedPreferences.edit(commit = true) {
            clear()
            putString(nonExtensionKey, nonExtensionKey)
        }
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with single registered watch and no sharedPreferences
        registeredWatches.value = listOf(dummyWatch1)
        sharedPreferences.edit(commit = true) {
            clear()
        }
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with no registered watch and extension sharedPreferences
        registeredWatches.value = emptyList()
        sharedPreferences.edit(commit = true) {
            remove(nonExtensionKey)
            SyncPreferences.ALL_PREFS.forEach {
                putInt(it, 1)
            }
        }
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with no registered watch and app sharedPreferences
        registeredWatches.value = emptyList()
        sharedPreferences.edit(commit = true) {
            clear()
            putString(nonExtensionKey, nonExtensionKey)
        }
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with no registered watch and no sharedPreferences
        registeredWatches.value = emptyList()
        sharedPreferences.edit(commit = true) {
            clear()
        }
        viewModel.resetExtensionSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())
    }

    @Test
    fun `resetAppSettings calls onComplete`() {
        val onComplete: (Boolean) -> Unit = mockk(relaxed = true)
        viewModel.resetAppSettings(
            { },
            onComplete
        )
        shadowOf(getMainLooper()).idle()
        verify(exactly = 1) { onComplete(any()) }
    }

    @Test
    fun `resetAppSettings resets app settings`() {
        val nonExtensionKey = "dummy-key"
        sharedPreferences.edit(commit = true) {
            putString(nonExtensionKey, nonExtensionKey)
            SyncPreferences.ALL_PREFS.forEach {
                putInt(it, 1)
            }
        }
        viewModel.resetAppSettings({ }, { })
        shadowOf(getMainLooper()).idle()
        assertThat(sharedPreferences.contains(nonExtensionKey)).isFalse()
        SyncPreferences.ALL_PREFS.forEach {
            assertThat(sharedPreferences.contains(it)).isTrue()
        }
    }

    @Test
    fun `resetAppSettings onProgressChanged never exceeds MAX_PROGRESS`() {
        val nonExtensionKey = "dummy-key"
        var currentProgress = 0
        val onProgressChanged: (Int) -> Unit = {
            currentProgress = it
        }

        // Test with full sharedPreferences
        sharedPreferences.edit(commit = true) {
            putString(nonExtensionKey, nonExtensionKey)
            SyncPreferences.ALL_PREFS.forEach {
                putInt(it, 1)
            }
        }
        viewModel.resetAppSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with extension sharedPreferences
        sharedPreferences.edit(commit = true) {
            clear()
            SyncPreferences.ALL_PREFS.forEach {
                putInt(it, 1)
            }
        }
        viewModel.resetAppSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with app sharedPreferences
        sharedPreferences.edit(commit = true) {
            clear()
            putString(nonExtensionKey, nonExtensionKey)
        }
        viewModel.resetAppSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with no sharedPreferences
        sharedPreferences.edit(commit = true) {
            clear()
        }
        viewModel.resetAppSettings(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())
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
                watchManager.forgetWatch(ApplicationProvider.getApplicationContext(), it)
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
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with registered watch
        registeredWatches.value = listOf(dummyWatch1)
        viewModel.resetApp(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())

        // Test with no registered watches
        registeredWatches.value = emptyList()
        viewModel.resetApp(onProgressChanged, { })
        shadowOf(getMainLooper()).idle()
        assertThat(currentProgress).isAtMost(MAX_PROGRESS.toInt())
    }
}
