package com.boswelja.devicemanager.managespace.ui.sheets

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.sheet.BaseResetBottomSheet
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test

class ClearCacheBottomSheetTest {

    private lateinit var scenario: FragmentScenario<ClearCacheBottomSheet>

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

    @Test
    fun uiFlowCompletes() {
        onView(withId(R.id.button)).perform(ViewActions.click())
        scenario.onFragment {
            // Ensure pending transactions are complete
            it.childFragmentManager.executePendingTransactions()

            // Check whether the current fragment is the end of the flow
            Truth.assertThat(it.childFragmentManager.findFragmentById(R.id.fragment_holder))
                .isInstanceOf(BaseResetBottomSheet.ConfirmationFragment::class.java)
        }

        // Check the button text is right to validate we are on the end fragment
        onView(withId(R.id.button)).check(matches(ViewMatchers.withText(R.string.button_done)))
    }
}
