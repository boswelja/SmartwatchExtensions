package com.boswelja.devicemanager.managespace.ui.sheets

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import org.junit.Before
import org.junit.Test

class ResetAppSettingsBottomSheetTest {

    private lateinit var scenario: FragmentScenario<ResetAppSettingsBottomSheet>

    @Before
    fun setUp() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_App)
    }

    @Test
    fun confirmationUiIsCompletelyDisplayed() {
        // Sheet should start on confirmation fragment, no need to do anything
        onView(withId(R.id.title)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.desc)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.button)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun progressUiIsCompletelyDisplayed() {
        // Sheet starts on confirmation fragment, need to navigate to progress fragment
        scenario.onFragment {
            it.showProgress()
        }

        onView(withId(R.id.title)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.desc)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.progress)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun resultsUiIsCompletelyDisplayed() {
        // Sheet starts on confirmation fragment, need to navigate to result fragment
        scenario.onFragment {
            it.showResults()
        }

        onView(withId(R.id.title)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.desc)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.button)).check(matches(isCompletelyDisplayed()))
    }
}
