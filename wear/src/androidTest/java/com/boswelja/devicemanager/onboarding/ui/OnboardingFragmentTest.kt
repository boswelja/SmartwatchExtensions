package com.boswelja.devicemanager.onboarding.ui

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import org.junit.Before
import org.junit.Test

class OnboardingFragmentTest {

    private lateinit var fragmentScenario: FragmentScenario<OnboardingFragment>

    @Before
    fun setUp() {
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.AppTheme)
    }

    @Test
    fun uiElementsVisible() {
        // Welcome view
        onView(withId(R.id.welcome_view_holder)).perform(scrollTo())
        onView(withId(R.id.app_icon_view)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.welcome_text_view)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.app_name_view)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.app_icon_view)).check(matches(isCompletelyDisplayed()))

        // Phone setup instructions
        onView(withId(R.id.phone_setup_helper_view)).perform(scrollTo())
        onView(withId(R.id.phone_setup_instructions_text)).check(matches(isCompletelyDisplayed()))

        // Watch register instructions
        onView(withId(R.id.setup_view_holder)).perform(scrollTo())
        onView(withId(R.id.setup_device_name_text)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.setup_instructions_text)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.auto_setup_text)).check(matches(isCompletelyDisplayed()))

        // Manual check view
        onView(withId(R.id.manual_check_view_holder)).perform(scrollTo())
        onView(withId(R.id.manual_check_text)).check(matches(isCompletelyDisplayed()))
    }
}
