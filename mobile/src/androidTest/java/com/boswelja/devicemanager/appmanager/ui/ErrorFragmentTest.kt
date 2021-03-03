package com.boswelja.devicemanager.appmanager.ui

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import org.junit.Before
import org.junit.Test

class ErrorFragmentTest {

    private lateinit var scenario: FragmentScenario<ErrorFragment>

    @Before
    fun setUp() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_App)
    }

    @Test
    fun viewsVisible() {
        onView(withId(R.id.icon)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.title)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.desc)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.retry_button)).check(matches(isCompletelyDisplayed()))
    }
}
