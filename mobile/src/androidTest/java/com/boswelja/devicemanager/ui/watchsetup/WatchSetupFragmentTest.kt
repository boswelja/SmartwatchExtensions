/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.watchsetup

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.watchsetup.ui.WatchSetupFragment
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Test

class WatchSetupFragmentTest {

    private fun createScenario(): FragmentScenario<WatchSetupFragment> {
        val scenario =
            launchFragmentInContainer<WatchSetupFragment>(themeResId = R.style.AppTheme)
        Thread.sleep(500)
        return scenario
    }

    @Test
    fun testViewVisibility() {
        createScenario()
        onView(withId(R.id.refresh_button)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun testHelpMessage() {
        val scenario = createScenario()
        val helpMessage = "This is a help message string"
        scenario.onFragment {
            it.setHelpMessage(helpMessage)
        }
        onView(withId(R.id.help_text_view)).check(
            matches(
                allOf(
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                    withText(helpMessage)
                )
            )
        )
        onView(withId(R.id.watch_setup_recyclerview)).check(matches(not(isEnabled())))

        scenario.onFragment {
            it.hideHelpMessage()
        }
        onView(withId(R.id.help_text_view)).check(
            matches(
                withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)
            )
        )
        onView(withId(R.id.watch_setup_recyclerview)).check(matches(isEnabled()))
    }

    @Test
    fun testSetLoading() {
        val scenario = createScenario()

        scenario.onFragment {
            it.setLoading(true)
        }
        onView(withId(R.id.watch_setup_recyclerview)).check(matches(not(isEnabled())))
        onView(withId(R.id.refresh_button)).check(matches(not(isEnabled())))
        onView(withId(R.id.progress_bar)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

        scenario.onFragment {
            it.setLoading(false)
        }
        onView(withId(R.id.watch_setup_recyclerview)).check(matches(isEnabled()))
        onView(withId(R.id.refresh_button)).check(matches(isEnabled()))
        onView(withId(R.id.progress_bar)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)))
    }
}
