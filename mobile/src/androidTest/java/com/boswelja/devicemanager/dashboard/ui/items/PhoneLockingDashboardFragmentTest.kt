package com.boswelja.devicemanager.dashboard.ui.items

import android.content.pm.ActivityInfo
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import org.hamcrest.Matchers.not
import org.junit.Test

class PhoneLockingDashboardFragmentTest {

    private fun createScenario(
        landscape: Boolean = false
    ): FragmentScenario<PhoneLockingDashboardFragment> {
        val scenario = launchFragmentInContainer<PhoneLockingDashboardFragment>(
            themeResId = R.style.`Theme.App`
        )
        if (landscape) {
            scenario.onFragment {
                it.activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
        return scenario
    }

    @Test
    fun portraitViewsVisible() {
        createScenario()
        onView(withId(R.id.settings_action))
            .check(matches(isCompletelyDisplayed()))
            .check(matches(isClickable()))
        onView(withId(R.id.item_content))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun landscapeViewsVisible() {
        createScenario(landscape = true)
        onView(withId(R.id.settings_action))
            .check(matches(isCompletelyDisplayed()))
            .check(matches(isClickable()))
        onView(withId(R.id.item_content))
            .check(matches(not(isDisplayed())))
    }
}
