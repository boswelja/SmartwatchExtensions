/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import android.content.SharedPreferences
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.common.References.REQUEST_RESET_APP
import com.boswelja.devicemanager.watchmanager.communication.WearOSConnectionManager
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class WatchManagerTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @RelaxedMockK
    lateinit var watchPreferenceManager: WatchPreferenceManager

    @RelaxedMockK
    lateinit var selectedWatchHandler: SelectedWatchHandler

    @RelaxedMockK
    lateinit var connectionManager: WearOSConnectionManager

    @RelaxedMockK
    lateinit var analytics: Analytics

    @RelaxedMockK
    lateinit var database: WatchDatabase

    private lateinit var watchManager: WatchManager

    @SpyK
    var sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())

    @Before
    fun setUp() {
        mockkObject(Utils)
        MockKAnnotations.init(this)

        every { database.isOpen } returns true

        watchManager = WatchManager(
            ApplicationProvider.getApplicationContext(),
            watchPreferenceManager,
            selectedWatchHandler,
            connectionManager,
            analytics,
            database
        )
    }

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun `getAvailableWatches calls the underlying ConnectionManager`() {
        watchManager.getAvailableWatches()
        verify(exactly = 1) { connectionManager.getAvailableWatches() }
    }

    @Test
    fun `requestResetWatch calls the underlying ConnectionManager`() {
        val id = "watch-id"
        watchManager.requestResetWatch(id)
        verify(exactly = 1) { connectionManager.sendMessage(id, REQUEST_RESET_APP) }
    }
}
