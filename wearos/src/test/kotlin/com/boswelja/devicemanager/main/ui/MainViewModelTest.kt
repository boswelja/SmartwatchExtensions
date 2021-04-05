package com.boswelja.devicemanager.main.ui

import android.content.SharedPreferences
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.capability.CapabilityUpdater
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        mockkConstructor(CapabilityUpdater::class)
        every { anyConstructed<CapabilityUpdater>().updateCapabilities() } answers { }

        sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `isRegistered correctly updates on ViewModel init`() {
        // Check with a 'valid' ID
        sharedPreferences.edit(commit = true) { putString(PHONE_ID_KEY, "id") }
        var viewModel = getViewModel()
        assertThat(viewModel.isRegistered.getOrAwaitValue()).isTrue()

        // Check with no ID
        sharedPreferences.edit(commit = true) { putString(PHONE_ID_KEY, "") }
        viewModel = getViewModel()
        assertThat(viewModel.isRegistered.getOrAwaitValue()).isFalse()
    }

    @Test
    fun `isRegistered correctly updates after ViewModel init`() {
        val viewModel = getViewModel()

        // Check with a 'valid' ID
        sharedPreferences.edit(commit = true) { putString(PHONE_ID_KEY, "id") }
        assertThat(viewModel.isRegistered.getOrAwaitValue()).isTrue()

        // Check with no ID
        sharedPreferences.edit(commit = true) { putString(PHONE_ID_KEY, "") }
        assertThat(viewModel.isRegistered.getOrAwaitValue()).isFalse()
    }

    @Test
    fun `Capabilities are updated on ViewModel init`() {
        getViewModel()
        verify(exactly = 1) { anyConstructed<CapabilityUpdater>().updateCapabilities() }
    }
    private fun getViewModel(): MainViewModel = MainViewModel(
        ApplicationProvider.getApplicationContext(),
        sharedPreferences
    )
}
