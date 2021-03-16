package com.boswelja.devicemanager.dndsync.ui

import android.content.SharedPreferences
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class DnDSyncPreferenceViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: DnDSyncPreferenceViewModel
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        viewModel = DnDSyncPreferenceViewModel(ApplicationProvider.getApplicationContext())
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            ApplicationProvider.getApplicationContext()
        )
    }

    @After
    fun tearDown() {
        sharedPreferences.edit(commit = true) { clear() }
    }

    @Test
    fun `No Sync updates drawableRes correctly`() {
        sharedPreferences.edit(commit = true) {
            putBoolean(DND_SYNC_TO_PHONE_KEY, false)
            putBoolean(DND_SYNC_TO_WATCH_KEY, false)
        }
        viewModel.drawableRes.getOrAwaitValue {
            assertThat(it).isEqualTo(R.drawable.ic_dnd_sync_none)
        }
    }

    @Test
    fun `Phone Sync only updates drawableRes correctly`() {
        sharedPreferences.edit(commit = true) {
            putBoolean(DND_SYNC_TO_PHONE_KEY, true)
            putBoolean(DND_SYNC_TO_WATCH_KEY, false)
        }
        viewModel.drawableRes.getOrAwaitValue {
            assertThat(it).isEqualTo(R.drawable.ic_dnd_sync_to_phone)
        }
    }

    @Test
    fun `Watch Sync only updates drawableRes correctly`() {
        sharedPreferences.edit(commit = true) {
            putBoolean(DND_SYNC_TO_PHONE_KEY, false)
            putBoolean(DND_SYNC_TO_WATCH_KEY, true)
        }
        viewModel.drawableRes.getOrAwaitValue {
            assertThat(it).isEqualTo(R.drawable.ic_dnd_sync_to_watch)
        }
    }

    @Test
    fun `Both Sync updates drawableRes correctly`() {
        sharedPreferences.edit(commit = true) {
            putBoolean(DND_SYNC_TO_PHONE_KEY, true)
            putBoolean(DND_SYNC_TO_WATCH_KEY, true)
        }
        viewModel.drawableRes.getOrAwaitValue {
            assertThat(it).isEqualTo(R.drawable.ic_dnd_sync_both)
        }
    }
}
