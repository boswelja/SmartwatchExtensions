package com.boswelja.devicemanager.ui.main

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.boswelja.devicemanager.R
import org.junit.Test

class MainFragmentTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun createScenario(): FragmentScenario<MainFragment> {
        return launchFragmentInContainer(themeResId = R.style.AppTheme)
    }

    @Test
    fun testUpdatingAdapterData() {
        val scenario = createScenario()
        var batteryPercent = 25
        scenario.onFragment {
            it.updateBatterySyncView(true, batteryPercent)
        }
        Thread.sleep(100)
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withText(
                        context.getString(R.string.phone_battery_percent, batteryPercent.toString())))))
        batteryPercent = 75
        scenario.onFragment {
            it.updateBatterySyncView(true, batteryPercent)
        }
        Thread.sleep(100)
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withText(
                        context.getString(R.string.phone_battery_percent, batteryPercent.toString())))))
        scenario.onFragment {
            it.updatePhoneLockingView(true)
        }
        Thread.sleep(100)
        onView(withId(R.id.recycler_view))
                .check(matches(hasDescendant(withText(context.getString(R.string.lock_phone_label)))))

    }
}