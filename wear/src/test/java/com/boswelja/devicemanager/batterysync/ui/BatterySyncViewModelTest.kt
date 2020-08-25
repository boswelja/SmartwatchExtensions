/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.batterysync.ui

import android.content.SharedPreferences
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PERCENT_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_CONNECTED_KEY
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_NAME_KEY
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class BatterySyncViewModelTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  private lateinit var viewModel: BatterySyncViewModel
  private lateinit var sharedPreferences: SharedPreferences

  @Before
  fun setUp() {
    viewModel = BatterySyncViewModel(ApplicationProvider.getApplicationContext())
    sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
  }

  @Test
  fun `Changing Battery Sync preference updates corresponding LiveData`() {
    sharedPreferences.edit(commit = true) { putBoolean(BATTERY_SYNC_ENABLED_KEY, true) }
    viewModel.batterySyncEnabled.getOrAwaitValue { assertThat(it).isTrue() }
    sharedPreferences.edit(commit = true) { putBoolean(BATTERY_SYNC_ENABLED_KEY, false) }
    viewModel.batterySyncEnabled.getOrAwaitValue { assertThat(it).isFalse() }
  }

  @Test
  fun `Changing Battery Percent preference updates corresponding LiveData`() {
    sharedPreferences.edit(commit = true) { putInt(BATTERY_PERCENT_KEY, 0) }
    viewModel.batteryPercent.getOrAwaitValue { assertThat(it).isEqualTo(0) }
    sharedPreferences.edit(commit = true) { putInt(BATTERY_PERCENT_KEY, 100) }
    viewModel.batteryPercent.getOrAwaitValue { assertThat(it).isEqualTo(100) }
    sharedPreferences.edit(commit = true) { putInt(BATTERY_PERCENT_KEY, 50) }
    viewModel.batteryPercent.getOrAwaitValue { assertThat(it).isEqualTo(50) }
  }

  @Test
  fun `Phone Name preference changes update corresponding LiveData`() {
    sharedPreferences.edit(commit = true) { putString(PHONE_NAME_KEY, "Phone") }
    viewModel.phoneName.getOrAwaitValue { assertThat(it).isEqualTo("Phone") }
    sharedPreferences.edit(commit = true) { putString(PHONE_NAME_KEY, "Pixel 3") }
    viewModel.phoneName.getOrAwaitValue { assertThat(it).isEqualTo("Pixel 3") }
  }

  @Test
  fun `Phone Connected preference changes update corresponding LiveData`() {
    sharedPreferences.edit(commit = true) { putBoolean(PHONE_CONNECTED_KEY, false) }
    viewModel.phoneConnected.getOrAwaitValue { assertThat(it).isFalse() }
    sharedPreferences.edit(commit = true) { putBoolean(PHONE_CONNECTED_KEY, true) }
    viewModel.phoneConnected.getOrAwaitValue { assertThat(it).isTrue() }
  }
}
