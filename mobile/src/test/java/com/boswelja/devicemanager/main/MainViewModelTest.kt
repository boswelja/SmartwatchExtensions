/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.main

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class MainViewModelTest {

    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        viewModel = MainViewModel(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `Changing message count updates LiveData correctly`() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                ApplicationProvider.getApplicationContext()
            )
        sharedPreferences.edit(commit = true) { putInt(MessageDatabase.MESSAGE_COUNT_KEY, 0) }
        viewModel.messageCount.getOrAwaitValue { assertThat(it).isEqualTo(0) }
        sharedPreferences.edit(commit = true) { putInt(MessageDatabase.MESSAGE_COUNT_KEY, 5) }
        viewModel.messageCount.getOrAwaitValue { assertThat(it).isEqualTo(5) }
        sharedPreferences.edit(commit = true) { putInt(MessageDatabase.MESSAGE_COUNT_KEY, 1) }
        viewModel.messageCount.getOrAwaitValue { assertThat(it).isEqualTo(1) }
    }
}
