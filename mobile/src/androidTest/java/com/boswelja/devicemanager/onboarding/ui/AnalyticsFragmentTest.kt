package com.boswelja.devicemanager.onboarding.ui

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class AnalyticsFragmentTest {

    private lateinit var navController: NavHostController
    private lateinit var fragmentScenario: FragmentScenario<AnalyticsFragment>

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_App)
        fragmentScenario.onFragment {
            navController.setGraph(R.navigation.onboarding_graph)
            Navigation.setViewNavController(it.requireView(), navController)
            // Ensure we're starting on the right destination
            navController.navigate(R.id.to_analyticsFragment)
        }
    }

    @Test
    fun uiElementsVisible() {
        onView(withId(R.id.icon)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.title)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.content)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.next_button)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.priv_policy_button)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.send_analytics_checkbox)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun navigateToSetupFragment() {
        onView(withId(R.id.next_button)).perform(ViewActions.click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.watchSetupFragment)
    }
}
