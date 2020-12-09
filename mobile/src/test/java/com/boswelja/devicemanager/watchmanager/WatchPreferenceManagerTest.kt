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
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.common.preference.SyncPreferences
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.IntPreference
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.gms.wearable.DataClient
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class WatchPreferenceManagerTest {

    private val coroutineScope = TestCoroutineScope()

    private val dummyWatch = Watch("an-id-1234", "Watch 1", null)
    private val dummyPreference = IntPreference(dummyWatch.id, "preference", 123)

    @RelaxedMockK
    lateinit var dataClient: DataClient

    @RelaxedMockK
    lateinit var database: WatchDatabase

    @SpyK
    var sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())

    private lateinit var watchPreferenceManager: WatchPreferenceManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { database.isOpen } returns true

        watchPreferenceManager =
            WatchPreferenceManager(
                ApplicationProvider.getApplicationContext(),
                coroutineScope = coroutineScope,
                dataClient = dataClient,
                sharedPreferences = sharedPreferences,
                database = database
            )
    }

    @After
    fun tearDown() {
        coroutineScope.cleanupTestCoroutines()
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun `clearLocalPreferences only clears watch-specific preferences`() {
        // Populate SharedPreferences
        val excludeKey = "i_shouldnt_be_wiped"
        sharedPreferences.edit(commit = true) {
            SyncPreferences.BOOL_PREFS.forEach { putBoolean(it, true) }
            SyncPreferences.INT_PREFS.forEach { putInt(it, 1) }
            putString(excludeKey, excludeKey)
        }

        watchPreferenceManager.clearLocalPreferences(true)
        assertThat(sharedPreferences.all.count()).isEqualTo(1)
        assertThat(sharedPreferences.getString(excludeKey, "")).isEqualTo(excludeKey)
    }

    @Test
    fun `Updating a preference in the database forwards the request correctly`() {
        watchPreferenceManager.updatePreferenceInDatabase(
            dummyPreference.watchId, dummyPreference.key, dummyPreference.value
        )
        verify(exactly = 1) {
            database.updatePrefInDatabase(
                dummyPreference.watchId, dummyPreference.key, dummyPreference.value
            )
        }
    }

    @Test
    fun `Updating a preference on a watch returns the correct value`(): Unit =
        runBlocking {
            // Any watch-specific preference should send a request and return a result
            SyncPreferences.ALL_PREFS.forEach {
                val result = watchPreferenceManager.updatePreferenceOnWatch(dummyWatch.id, it)
                assertThat(result).isNotNull()
            }

            // Any preference outside of SyncPreferences.ALL_PREFS should not sync to the watch.
            val result =
                watchPreferenceManager.updatePreferenceOnWatch(dummyWatch.id, dummyPreference.key)
            assertThat(result).isNull()
        }

    @Test
    fun `updateAllPreferencesOnWatch gets preferences from the database`(): Unit =
        runBlocking {
            val result = watchPreferenceManager.updateAllPreferencesOnWatch(dummyWatch.id)
            verify(exactly = 1) { database.boolPrefDao().getAllForWatch(dummyWatch.id) }
            verify(exactly = 1) { database.intPrefDao().getAllForWatch(dummyWatch.id) }
            verify(exactly = 1) { dataClient.putDataItem(any()) }
            assertThat(result).isNotNull()
        }

    @Test
    fun `updateAllPreferencesOnWatch returns null when watchId is null or empty`(): Unit =
        runBlocking {
            var result = watchPreferenceManager.updateAllPreferencesOnWatch("")
            verify(exactly = 0) { dataClient.putDataItem(any()) }
            assertThat(result).isNull()
            result = watchPreferenceManager.updateAllPreferencesOnWatch(null)
            verify(exactly = 0) { dataClient.putDataItem(any()) }
            assertThat(result).isNull()
        }

    @Test
    fun `clearPreferencesForWatch clears preferences from the database`(): Unit =
        runBlocking {
            val result = watchPreferenceManager.clearPreferencesForWatch(dummyWatch.id)
            verify { database.intPrefDao().deleteAllForWatch(dummyWatch.id) }
            verify { database.boolPrefDao().deleteAllForWatch(dummyWatch.id) }
            verify(exactly = 1) { dataClient.putDataItem(any()) }
            assertThat(result).isTrue()
        }

    @Test
    fun `clearPreferencesForWatch returns null if watchId is null or empty`(): Unit =
        runBlocking {
            var result = watchPreferenceManager.clearPreferencesForWatch("")
            assertThat(result).isFalse()
            result = watchPreferenceManager.clearPreferencesForWatch(null)
            assertThat(result).isFalse()

            verify(exactly = 0) { database.intPrefDao().deleteAllForWatch(any()) }
            verify(exactly = 0) { database.boolPrefDao().deleteAllForWatch(any()) }
            verify(exactly = 0) { dataClient.putDataItem(any()) }
        }

    @Test
    fun `updatePreferenceOnWatch creates a request and returns the correct value`(): Unit =
        runBlocking {
            SyncPreferences.ALL_PREFS.forEach {
                val result = watchPreferenceManager.updatePreferenceOnWatch(dummyWatch.id, it)
                assertThat(result).isNotNull()
            }
            verify(exactly = SyncPreferences.ALL_PREFS.count()) { dataClient.putDataItem(any()) }
            val result =
                watchPreferenceManager.updatePreferenceOnWatch(dummyWatch.id, dummyPreference.key)
            assertThat(result).isNull()
        }
}
