/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.extensions.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
class ExtensionsViewModelTest {

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ExtensionsViewModel

    @MockK(relaxed = true)
    private lateinit var nodeClient: NodeClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = ExtensionsViewModel(ApplicationProvider.getApplicationContext(), nodeClient)
    }

    @Test
    fun `Requesting phone connected status calls NodeClient`() {
        viewModel.updatePhoneConnectedStatus()
        verify(exactly = 1) { nodeClient.connectedNodes }
    }
}
