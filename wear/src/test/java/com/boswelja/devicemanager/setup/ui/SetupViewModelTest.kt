package com.boswelja.devicemanager.setup.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.getOrAwaitValue
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class SetupViewModelTest {

  @get:Rule
  val instantExecutorRule = InstantTaskExecutorRule()

  @MockK(relaxed = true)
  private lateinit var nodeClient: NodeClient
  @MockK(relaxed = true)
  private lateinit var messageClient: MessageClient

  private lateinit var viewModel: SetupViewModel

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    every { nodeClient.localNode.addOnCompleteListener(any()) }
    viewModel = SetupViewModel(ApplicationProvider.getApplicationContext(), nodeClient, messageClient)
  }

  @Test
  fun `Creating ViewModel gets local node information`() {
    verify { nodeClient.localNode }
  }
}