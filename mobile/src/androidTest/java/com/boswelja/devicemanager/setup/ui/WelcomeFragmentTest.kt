package com.boswelja.devicemanager.setup.ui

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class WelcomeFragmentTest {

    private lateinit var navController: NavHostController
    private lateinit var fragmentScenario: FragmentScenario<WelcomeFragment>

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_App)
        fragmentScenario.onFragment {
            navController.setGraph(R.navigation.setup_graph)
            Navigation.setViewNavController(it.requireView(), navController)
        }
    }

    @Test
    fun navigateToSetupFragment() {
        onView(withId(R.id.get_started_button)).perform(click())
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.watchSetupFragment)
    }
}
