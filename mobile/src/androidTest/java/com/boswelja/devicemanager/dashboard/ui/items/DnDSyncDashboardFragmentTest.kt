package com.boswelja.devicemanager.dashboard.ui.items

import android.content.pm.ActivityInfo
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.dndsync.ui.DnDSyncPreferenceWidgetFragment
import com.google.common.truth.Truth
import org.junit.Test

class DnDSyncDashboardFragmentTest {

    private fun createScenario(
        landscape: Boolean = false
    ): FragmentScenario<DnDSyncDashboardFragment> {
        val scenario = launchFragmentInContainer<DnDSyncDashboardFragment>(
            themeResId = R.style.Theme_App
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
    }

    @Test
    fun landscapeViewsVisible() {
        createScenario(landscape = true)
        onView(withId(R.id.settings_action))
            .check(matches(isCompletelyDisplayed()))
            .check(matches(isClickable()))
    }

    @Test
    fun widgetLoaded() {
        val scenario = createScenario()
        scenario.onFragment { fragment ->
            Truth.assertThat(
                fragment.childFragmentManager.fragments.any {
                    it is DnDSyncPreferenceWidgetFragment
                }
            ).isTrue()
        }
    }
}
