package com.boswelja.devicemanager.dndsync.ui

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.TestExtensions.withDrawable
import org.junit.After
import org.junit.Before
import org.junit.Test

class DnDSyncPreferenceWidgetFragmentTest {

    private lateinit var sharedPreferences: SharedPreferences

    private fun createScenario(
        landscape: Boolean = false
    ): FragmentScenario<DnDSyncPreferenceWidgetFragment> {
        val scenario = launchFragmentInContainer<DnDSyncPreferenceWidgetFragment>(
            themeResId = R.style.AppTheme
        )
        if (landscape) {
            scenario.onFragment {
                it.activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }
        return scenario
    }

    @Before
    fun setUp() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            InstrumentationRegistry.getInstrumentation().context
        )
    }

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun portraitViewVisibility() {
        createScenario()
        onView(withId(R.id.dnd_sync_status_indicator))
            .check(matches(isCompletelyDisplayed()))
        onView(withDrawable(R.drawable.ic_dnd_phone))
            .check(matches(isCompletelyDisplayed()))
        onView(withDrawable(R.drawable.ic_dnd_watch))
            .check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun landscapeViewVisibility() {
        createScenario(landscape = true)
        onView(withId(R.id.dnd_sync_status_indicator))
            .check(matches(isCompletelyDisplayed()))
        onView(withDrawable(R.drawable.ic_dnd_phone))
            .check(matches(isCompletelyDisplayed()))
        onView(withDrawable(R.drawable.ic_dnd_watch))
            .check(matches(isCompletelyDisplayed()))
    }
}
