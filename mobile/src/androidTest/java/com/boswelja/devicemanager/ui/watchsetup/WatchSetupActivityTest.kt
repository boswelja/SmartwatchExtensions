/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.watchsetup

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.watchsetup.WatchSetupActivity.Companion.EXTRA_SKIP_WELCOME
import org.junit.Rule
import org.junit.Test

class WatchSetupActivityTest {

    @get:Rule
    val activityTestRule = ActivityTestRule(WatchSetupActivity::class.java, true, false)

    @Test
    fun testWelcomeFragmentVisible() {
        activityTestRule.launchActivity(null)

        onView(withId(R.id.app_icon)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.welcome_to_text)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.app_name_text)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.get_started_button)).check(matches(isCompletelyDisplayed()))

        activityTestRule.finishActivity()
    }

    @Test
    fun testGetStartedTransition() {
        activityTestRule.launchActivity(null)

        onView(withId(R.id.get_started_button)).perform(click())
        onView(withId(R.id.watch_setup_recyclerview)).check(matches(isDisplayed()))

        activityTestRule.finishActivity()
    }

    @Test
    fun skipWelcomeFragmentIntent() {
        Intent().apply {
            putExtra(EXTRA_SKIP_WELCOME, true)
        }.also {
            activityTestRule.launchActivity(it)
        }

        onView(withId(R.id.watch_setup_recyclerview)).check(matches(isDisplayed()))
    }
}
