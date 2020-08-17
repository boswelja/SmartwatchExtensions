package com.boswelja.devicemanager.appmanager.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.boswelja.devicemanager.common.appmanager.References.START_SERVICE
import com.boswelja.devicemanager.common.appmanager.References.STOP_SERVICE
import com.google.android.gms.wearable.MessageClient
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppManagerViewModelTest {

    private val watchId = "123456"

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK(relaxed = true)
    private lateinit var messageClient: MessageClient

    private lateinit var viewModel: AppManagerViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = AppManagerViewModel(messageClient)
        viewModel.watchId = watchId
    }

    @Test
    fun `App Manager stop request not sent when not allowed`() {
        viewModel.startAppManagerService()
        verify(exactly = 1) { messageClient.sendMessage(watchId, START_SERVICE, null) }

        viewModel.canStopAppManagerService = false
        viewModel.tryStopAppManagerService()
        verify(exactly = 0) { messageClient.sendMessage(any(), STOP_SERVICE, null) }
    }

    @Test
    fun `App Manager stop request sent when allowed`() {
        viewModel.startAppManagerService()
        verify(exactly = 1) { messageClient.sendMessage(watchId, START_SERVICE, null) }

        viewModel.canStopAppManagerService = true
        viewModel.tryStopAppManagerService()
        verify(exactly = 1) { messageClient.sendMessage(any(), STOP_SERVICE, null) }
    }

    @Test
    fun `Toggling canStopAppManagerService works`() {
        viewModel.startAppManagerService()
        verify(exactly = 1) { messageClient.sendMessage(watchId, START_SERVICE, null) }

        viewModel.canStopAppManagerService = false
        viewModel.tryStopAppManagerService()
        verify(exactly = 0) { messageClient.sendMessage(any(), STOP_SERVICE, null) }

        viewModel.canStopAppManagerService = true
        viewModel.tryStopAppManagerService()
        verify(exactly = 1) { messageClient.sendMessage(any(), STOP_SERVICE, null) }
    }
}