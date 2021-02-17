/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.batterysync

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.edit
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.database.WatchBatteryStats
import com.boswelja.devicemanager.batterysync.ui.BatterySyncPreferenceWidgetFragment
import com.boswelja.devicemanager.common.preference.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.watchmanager.WatchManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BatterySyncPreferenceWidgetFragmentTest {

    private val dummyStats = WatchBatteryStats("watch-id", 95, false)

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var scenario: FragmentScenario<BatterySyncPreferenceWidgetFragment>

    @RelaxedMockK private lateinit var watchManager: WatchManager

    @Before
    fun setUp() {
        sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        MockKAnnotations.init(this)
        mockkObject(WatchManager.Companion)
        every { WatchManager.Companion.getInstance(any()) } returns watchManager
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_App)
    }

    @Test
    fun portraitDisabledViewVisibility() {
        setBatterySyncEnabled(false)
        scenario.onFragment {
            it.showBatterySyncDisabled()
        }
        onView(withId(R.id.watch_battery_percent)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.watch_battery_indicator)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.last_updated_time))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)))
    }

    @Test
    fun landscapeDisabledViewVisibility() {
        setBatterySyncEnabled(false)
        scenario.onFragment {
            it.activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            it.showBatterySyncDisabled()
        }
        onView(withId(R.id.watch_battery_percent)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.watch_battery_indicator)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.last_updated_time))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)))
    }

    @Test
    fun landscapeStatsViewVisibility() {
        setBatterySyncEnabled(true)
        scenario.onFragment {
            it.activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            it.updateBatteryStats(dummyStats)
        }
        onView(withId(R.id.watch_battery_percent)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.watch_battery_indicator)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.last_updated_time)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun portraitStatsViewVisibility() {
        setBatterySyncEnabled(true)
        scenario.onFragment {
            it.updateBatteryStats(dummyStats)
        }
        onView(withId(R.id.watch_battery_percent)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.watch_battery_indicator)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.last_updated_time)).check(matches(isCompletelyDisplayed()))
    }

    private fun setBatterySyncEnabled(isEnabled: Boolean) {
        sharedPreferences.edit(commit = true) { putBoolean(BATTERY_SYNC_ENABLED_KEY, isEnabled) }
    }
}
