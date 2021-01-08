/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.onboarding.ui

import android.content.SharedPreferences
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.common.setup.References.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.common.setup.References.WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class OnboardingViewModelTest {

    private val dummyPhone = object : Node {
        override fun isNearby(): Boolean = true

        override fun getDisplayName(): String = "Phone"

        override fun getId(): String = "dummy-phone-id"
    }

    private val watchRegisteredEvent = object : MessageEvent {
        override fun getSourceNodeId(): String = dummyPhone.id

        override fun getRequestId(): Int = 0

        override fun getPath(): String = WATCH_REGISTERED_PATH

        override fun getData(): ByteArray? = null
    }

    private val connectedNodeTask = ConnectedNodeDummy(listOf(dummyPhone))

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @MockK(relaxed = true)
    private lateinit var nodeClient: NodeClient

    @MockK(relaxed = true)
    private lateinit var messageClient: MessageClient

    private lateinit var viewModel: OnboardingViewModel
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            ApplicationProvider.getApplicationContext()
        )

        every { nodeClient.connectedNodes } returns connectedNodeTask

        viewModel = OnboardingViewModel(
            ApplicationProvider.getApplicationContext(),
            nodeClient,
            messageClient,
            sharedPreferences
        )
    }

    @After
    fun tearDown() {
        sharedPreferences.edit(commit = true) { clear() }
    }

    @Test
    fun `Creating ViewModel gets local node information`() {
        verify { nodeClient.localNode }
    }

    @Test
    fun `Receiving WATCH_REGISTERED_PATH saves the phone ID and notifies observers`() {
        // 'Send' event
        viewModel.messageListener.onMessageReceived(watchRegisteredEvent)
        assertThat(sharedPreferences.getString(PHONE_ID_KEY, "")).isEqualTo(dummyPhone.id)
        viewModel.onWatchRegistered.getOrAwaitValue {
            assertThat(it).isTrue()
        }
    }

    @Test
    fun `Refreshing registered status sends CHECK_WATCH_REGISTERED_PATH to phone`() {
        viewModel.refreshRegisteredStatus()
        verify(exactly = 1) {
            messageClient.sendMessage(dummyPhone.id, CHECK_WATCH_REGISTERED_PATH, null)
        }
    }
}
