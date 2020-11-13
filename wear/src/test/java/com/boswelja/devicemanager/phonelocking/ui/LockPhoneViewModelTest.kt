package com.boswelja.devicemanager.phonelocking.ui

import android.content.SharedPreferences
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.getOrAwaitValue
import com.boswelja.devicemanager.phoneconnectionmanager.References
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class LockPhoneViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                ApplicationProvider.getApplicationContext()
            )
    }

    @Test
    fun `Phone Name preference changes update corresponding LiveData`() {
        val viewModel = LockPhoneViewModel(ApplicationProvider.getApplicationContext())
        sharedPreferences.edit(commit = true) { putString(References.PHONE_NAME_KEY, "Phone") }
        viewModel.phoneName.getOrAwaitValue { Truth.assertThat(it).isEqualTo("Phone") }
        sharedPreferences.edit(commit = true) { putString(References.PHONE_NAME_KEY, "Pixel 3") }
        viewModel.phoneName.getOrAwaitValue { Truth.assertThat(it).isEqualTo("Pixel 3") }
    }

    @Test
    fun `Phone Connected preference changes update corresponding LiveData`() {
        val viewModel = LockPhoneViewModel(ApplicationProvider.getApplicationContext())
        sharedPreferences.edit(commit = true) { putBoolean(References.PHONE_CONNECTED_KEY, false) }
        viewModel.phoneConnected.getOrAwaitValue { Truth.assertThat(it).isFalse() }
        sharedPreferences.edit(commit = true) { putBoolean(References.PHONE_CONNECTED_KEY, true) }
        viewModel.phoneConnected.getOrAwaitValue { Truth.assertThat(it).isTrue() }
    }

    @Test
    fun `Phone connected LiveData starts with the correct value`() {
        sharedPreferences.edit(commit = true) { putBoolean(References.PHONE_CONNECTED_KEY, false) }
        var viewModel = LockPhoneViewModel(ApplicationProvider.getApplicationContext())
        Truth.assertThat(viewModel.phoneConnected.value).isFalse()

        sharedPreferences.edit(commit = true) { putBoolean(References.PHONE_CONNECTED_KEY, true) }
        viewModel = LockPhoneViewModel(ApplicationProvider.getApplicationContext())
        Truth.assertThat(viewModel.phoneConnected.value).isTrue()
    }
}
