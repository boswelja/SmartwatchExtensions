package com.boswelja.devicemanager.dashboard.ui.items

import android.content.pm.ActivityInfo
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.ui.BatterySyncPreferenceWidgetFragment
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BatterySyncDashboardFragmentTest {

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private lateinit var scenario: FragmentScenario<BatterySyncDashboardFragment>

    @Before
    fun setUp() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_App)
    }

    @Test
    fun portraitViewsVisible() {
        onView(withId(R.id.settings_action))
            .check(matches(isCompletelyDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun landscapeViewsVisible() {
        scenario.onFragment {
            it.activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        onView(withId(R.id.settings_action))
            .check(matches(isCompletelyDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun widgetLoaded() {
        scenario.onFragment { fragment ->
            assertThat(
                fragment.childFragmentManager.fragments.any {
                    it is BatterySyncPreferenceWidgetFragment
                }
            ).isTrue()
        }
    }
}
