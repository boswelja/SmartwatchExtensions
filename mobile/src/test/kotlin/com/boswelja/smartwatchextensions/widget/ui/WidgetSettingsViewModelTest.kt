package com.boswelja.smartwatchextensions.widget.ui

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class WidgetSettingsViewModelTest {

    @get:Rule val taskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Application

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `setShowBackground updates corresponding LiveData`() {
        val viewModel = WidgetSettingsViewModel(context)
        viewModel.setShowBackground(true)
        assertThat(viewModel.widgetBackgroundVisible.getOrAwaitValue()).isTrue()
        viewModel.setShowBackground(false)
        assertThat(viewModel.widgetBackgroundVisible.getOrAwaitValue()).isFalse()
    }

    @Test
    fun `setBackgroundOpacity updates corresponding LiveData`() {
        val viewModel = WidgetSettingsViewModel(context)
        viewModel.setBackgroundOpacity(0)
        assertThat(viewModel.widgetBackgroundOpacity.getOrAwaitValue()).isEqualTo(0)
        viewModel.setBackgroundOpacity(100)
        assertThat(viewModel.widgetBackgroundOpacity.getOrAwaitValue()).isEqualTo(100)
        viewModel.setBackgroundOpacity(60)
        assertThat(viewModel.widgetBackgroundOpacity.getOrAwaitValue()).isEqualTo(60)
    }
}
