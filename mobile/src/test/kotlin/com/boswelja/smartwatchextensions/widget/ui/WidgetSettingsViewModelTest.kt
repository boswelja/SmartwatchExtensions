package com.boswelja.smartwatchextensions.widget.ui

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.getOrAwaitValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

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
        expectThat(viewModel.widgetBackgroundVisible.getOrAwaitValue()).isTrue()
        viewModel.setShowBackground(false)
        expectThat(viewModel.widgetBackgroundVisible.getOrAwaitValue()).isFalse()
    }

    @Test
    fun `setBackgroundOpacity updates corresponding LiveData`() {
        val viewModel = WidgetSettingsViewModel(context)
        viewModel.setBackgroundOpacity(0)
        expectThat(viewModel.widgetBackgroundOpacity.getOrAwaitValue()).isEqualTo(0)
        viewModel.setBackgroundOpacity(100)
        expectThat(viewModel.widgetBackgroundOpacity.getOrAwaitValue()).isEqualTo(100)
        viewModel.setBackgroundOpacity(60)
        expectThat(viewModel.widgetBackgroundOpacity.getOrAwaitValue()).isEqualTo(60)
    }
}
