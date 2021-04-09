package com.boswelja.devicemanager.main.ui

import android.os.Build
import androidx.datastore.core.DataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.PhoneState
import com.boswelja.devicemanager.capability.CapabilityUpdater
import com.boswelja.devicemanager.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
@ExperimentalCoroutinesApi
class MainViewModelTest {

    private lateinit var dataStore: DataStore<PhoneState>

    private val phoneState = MutableStateFlow(PhoneState())

    @Before
    fun setUp(): Unit = runBlocking {
        dataStore = mockk()
        mockkConstructor(CapabilityUpdater::class)
        every { anyConstructed<CapabilityUpdater>().updateCapabilities() } answers { }
        every { dataStore.data } returns phoneState
    }

    @Test
    fun `isRegistered is true on ViewModel init with ID set`(): Unit = runBlocking {
        updatePhoneId("id")
        val viewModel = getViewModel()
        assertThat(viewModel.isRegistered.getOrAwaitValue()).isTrue()
    }

    @Test
    fun `isRegistered is false on ViewModel init with no ID`(): Unit = runBlocking {
        updatePhoneId("")
        val viewModel = getViewModel()
        assertThat(viewModel.isRegistered.getOrAwaitValue()).isFalse()
    }

    @Test
    fun `isRegistered is true when ID set after ViewModel init`(): Unit = runBlocking {
        val viewModel = getViewModel()
        updatePhoneId("id")
        assertThat(viewModel.isRegistered.getOrAwaitValue()).isTrue()
    }

    @Test
    fun `isRegistered is false when ID cleared after ViewModel init`(): Unit = runBlocking {
        val viewModel = getViewModel()
        updatePhoneId("")
        assertThat(viewModel.isRegistered.getOrAwaitValue()).isFalse()
    }

    @Test
    fun `Capabilities are updated on ViewModel init`() {
        getViewModel()
        verify(exactly = 1) { anyConstructed<CapabilityUpdater>().updateCapabilities() }
    }

    private suspend fun updatePhoneId(id: String) {
        phoneState.emit(PhoneState(id = id))
    }

    private fun getViewModel(): MainViewModel = MainViewModel(
        ApplicationProvider.getApplicationContext(), dataStore
    )
}
