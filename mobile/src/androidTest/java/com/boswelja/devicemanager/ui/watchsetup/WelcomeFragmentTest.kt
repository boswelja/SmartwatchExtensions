package com.boswelja.devicemanager.ui.watchsetup

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import org.junit.Test

class WelcomeFragmentTest {

    private fun createScenario(): FragmentScenario<WelcomeFragment> =
            launchFragmentInContainer<WelcomeFragment>(themeResId = R.style.AppTheme)

    @Test
    fun testViewVisibility() {
        createScenario()

        onView(withId(R.id.app_icon)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.welcome_to_text)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.app_name_text)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.get_started_button)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun testFragmentLifecycle() {
        val scenario = createScenario()
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }
}