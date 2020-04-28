/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.batterysync

import android.view.View
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.TestExtensions.setText
import com.boswelja.devicemanager.TestExtensions.setVisibility
import org.junit.Test

class BatterySyncPreferenceWidgetFragmentTest {

    private fun createScenario(): FragmentScenario<BatterySyncPreferenceWidgetFragment> {
        return launchFragmentInContainer(themeResId = R.style.AppTheme)
    }

    @Test
    fun testViewVisibility() {
        createScenario()
        // Set all views to their largest size and make them visible.
        onView(withId(R.id.watch_battery_percent))
                .perform(setVisibility(View.VISIBLE))
                .perform(setText("Battery Sync Disabled"))
                .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.watch_battery_indicator))
                .perform(setVisibility(View.VISIBLE))
                .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.last_updated_time))
                .perform(setVisibility(View.VISIBLE))
                .perform(setText("Last updated less than a minute ago"))
                .check(matches(isCompletelyDisplayed()))
    }
}
