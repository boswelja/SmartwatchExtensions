/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.widget.ui

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.appsettings.ui.AppSettingsFragment
import com.boswelja.devicemanager.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class WidgetSettingsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: WidgetSettingsViewModel

    @Before
    fun setUp() {
        viewModel = WidgetSettingsViewModel(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `Toggling widget background preference updates LiveData correctly`() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                ApplicationProvider.getApplicationContext()
            )
        sharedPreferences.edit(commit = true) {
            putBoolean(AppSettingsFragment.SHOW_WIDGET_BACKGROUND_KEY, false)
        }
        viewModel.widgetBackgroundVisible.getOrAwaitValue { assertThat(it).isFalse() }

        sharedPreferences.edit(commit = true) {
            putBoolean(AppSettingsFragment.SHOW_WIDGET_BACKGROUND_KEY, true)
        }
        viewModel.widgetBackgroundVisible.getOrAwaitValue { assertThat(it).isTrue() }
    }

    @Test
    fun `Changing widget background opacity preference updates LiveData correctly`() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                ApplicationProvider.getApplicationContext()
            )
        sharedPreferences.edit(commit = true) {
            putInt(AppSettingsFragment.WIDGET_BACKGROUND_OPACITY_KEY, 0)
        }
        viewModel.widgetBackgroundOpacity.getOrAwaitValue { assertThat(it).isEqualTo(0) }

        sharedPreferences.edit(commit = true) {
            putInt(AppSettingsFragment.WIDGET_BACKGROUND_OPACITY_KEY, 100)
        }
        viewModel.widgetBackgroundOpacity.getOrAwaitValue { assertThat(it).isEqualTo(100) }

        sharedPreferences.edit(commit = true) {
            putInt(AppSettingsFragment.WIDGET_BACKGROUND_OPACITY_KEY, 50)
        }
        viewModel.widgetBackgroundOpacity.getOrAwaitValue { assertThat(it).isEqualTo(50) }
    }
}
