/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.setup.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.NodeClient
import io.mockk.MockKAnnotations
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

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    @MockK(relaxed = true)
    private lateinit var nodeClient: NodeClient
    @MockK(relaxed = true)
    private lateinit var messageClient: MessageClient

    private lateinit var viewModel: SetupViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel =
            SetupViewModel(ApplicationProvider.getApplicationContext(), nodeClient, messageClient)
    }

    @Test
    fun `Creating ViewModel gets local node information`() {
        verify { nodeClient.localNode }
    }
}
