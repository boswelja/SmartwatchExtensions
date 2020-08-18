/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appinfo.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.boswelja.devicemanager.appmanager.ui.getOrAwaitValue
import com.boswelja.devicemanager.common.References.REQUEST_APP_VERSION
import com.google.android.gms.wearable.MessageClient
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppInfoViewModelTest {

    private val watchId = "123456"

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK(relaxed = true)
    private lateinit var messageClient: MessageClient

    private lateinit var viewModel: AppInfoViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = AppInfoViewModel(messageClient)
    }

    @Test
    fun `Requesting watch version with empty watch ID fails`() {
        viewModel.requestUpdateWatchVersion("")
        viewModel.watchAppVersion.getOrAwaitValue {
            assertThat(it).isNull()
        }
    }

    @Test
    fun `Requesting watch version sends a request`() {
        viewModel.requestUpdateWatchVersion(watchId)
        verify(exactly = 1) { messageClient.sendMessage(watchId, REQUEST_APP_VERSION, null) }
    }
}
