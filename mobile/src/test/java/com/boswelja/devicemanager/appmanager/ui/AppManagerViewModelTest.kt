/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.common.appmanager.References.START_SERVICE
import com.boswelja.devicemanager.common.appmanager.References.STOP_SERVICE
import com.google.android.gms.wearable.MessageClient
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class AppManagerViewModelTest {

  private val watchId = "123456"

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  @MockK(relaxed = true)
  private lateinit var messageClient: MessageClient

  private lateinit var viewModel: AppManagerViewModel

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    viewModel = AppManagerViewModel(ApplicationProvider.getApplicationContext(), messageClient)
    viewModel.watchId = watchId
    verify { messageClient.addListener(any()) }
  }

  @Test
  fun `App Manager stop request not sent when not allowed`() {
    viewModel.startAppManagerService()
    verify(exactly = 1) { messageClient.sendMessage(watchId, START_SERVICE, null) }

    viewModel.canStopAppManagerService = false
    viewModel.tryStopAppManagerService()
    verify(exactly = 0) { messageClient.sendMessage(any(), STOP_SERVICE, null) }

    confirmVerified(messageClient)
  }

  @Test
  fun `App Manager stop request sent when allowed`() {
    viewModel.startAppManagerService()
    verify(exactly = 1) { messageClient.sendMessage(watchId, START_SERVICE, null) }

    viewModel.canStopAppManagerService = true
    viewModel.tryStopAppManagerService()
    verify(exactly = 1) { messageClient.sendMessage(any(), STOP_SERVICE, null) }

    confirmVerified(messageClient)
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

    confirmVerified(messageClient)
  }
}
