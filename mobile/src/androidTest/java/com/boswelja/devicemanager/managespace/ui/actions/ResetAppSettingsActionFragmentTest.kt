package com.boswelja.devicemanager.managespace.ui.actions

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.managespace.ui.sheets.ResetAppSettingsBottomSheet
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class ResetAppSettingsActionFragmentTest {

    private lateinit var scenario: FragmentScenario<ResetAppSettingsActionFragment>

    @Before
    fun setUp() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_App)
    }

    @Test
    fun uiIsCompletelyDisplayed() {
        onView(withId(R.id.title)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.desc)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.button)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun buttonOpensBottomSheet() {
        onView(withId(R.id.button)).perform(ViewActions.click())
        scenario.onFragment {
            // Ensure all pending transactions are executed
            it.childFragmentManager.executePendingTransactions()

            // Find fragment by tag. If it's not null, it exists and therefore must be visible
            assertThat(
                it.childFragmentManager
                    .findFragmentByTag(ResetAppSettingsBottomSheet::class.simpleName)
            ).isNotNull()
        }
    }
}
