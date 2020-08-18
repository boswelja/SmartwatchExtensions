/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WatchServiceLifecycleObserverTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  @MockK(relaxed = true)
  lateinit var lifecycleOwner: LifecycleOwner

  @MockK(relaxed = true)
  lateinit var viewModel: AppManagerViewModel

  private lateinit var lifecycle: LifecycleRegistry
  private lateinit var watchServiceLifecycleObserver: WatchServiceLifecycleObserver

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    lifecycle = LifecycleRegistry(lifecycleOwner)
    watchServiceLifecycleObserver = WatchServiceLifecycleObserver(viewModel)
    lifecycle.addObserver(watchServiceLifecycleObserver)
  }

  @After
  fun tearDown() {
    lifecycle.removeObserver(watchServiceLifecycleObserver)
    lifecycle.currentState = Lifecycle.State.DESTROYED
  }

  @Test
  fun `App Manager started & stopped with Lifecycle`() {
    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
    verify(exactly = 1) { viewModel.startAppManagerService() }

    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    verify(exactly = 1) { viewModel.tryStopAppManagerService() }

    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    verify(exactly = 1) { viewModel.canStopAppManagerService = true }
    verify { viewModel.tryStopAppManagerService() }

    confirmVerified(viewModel)
  }
}
