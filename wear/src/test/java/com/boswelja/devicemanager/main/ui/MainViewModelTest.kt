/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.main.ui

import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class MainViewModelTest {

    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                ApplicationProvider.getApplicationContext()
            )
    }

    @Test
    fun `isRegistered is false when no phone ID is stored`() {
        sharedPreferences.edit(commit = true) { remove(PHONE_ID_KEY) }
        val viewModel = MainViewModel(ApplicationProvider.getApplicationContext())
        assertThat(viewModel.isRegistered).isFalse()
    }

    @Test
    fun `isRegistered is false when stored phone ID is blank`() {
        sharedPreferences.edit(commit = true) { putString(PHONE_ID_KEY, "   ") }
        val viewModel = MainViewModel(ApplicationProvider.getApplicationContext())
        assertThat(viewModel.isRegistered).isFalse()
    }

    @Test
    fun `isRegistered is true when a phone ID is stored`() {
        sharedPreferences.edit(commit = true) { putString(PHONE_ID_KEY, "id123") }
        val viewModel = MainViewModel(ApplicationProvider.getApplicationContext())
        assertThat(viewModel.isRegistered).isTrue()
    }
}
