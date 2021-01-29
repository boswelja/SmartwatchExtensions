package com.boswelja.devicemanager.batterysync.widget.config

import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.watchmanager.communication.WearOSConnectionManager
import com.boswelja.devicemanager.watchmanager.database.WatchDatabase
import com.boswelja.devicemanager.watchmanager.item.Watch
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test

class BatteryWidgetConfigActivityTest {

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1", WearOSConnectionManager.PLATFORM)
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2", WearOSConnectionManager.PLATFORM)
    private val dummyWatch3 = Watch("an-id-3456", "Watch 3", WearOSConnectionManager.PLATFORM)

    private lateinit var scenario: ActivityScenario<BatteryWidgetConfigActivity>
    private lateinit var watchDatabase: WatchDatabase

    @Before
    fun setUp() {
        watchDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WatchDatabase::class.java
        ).build()

        mockkObject(WatchDatabase)
        every { WatchDatabase.getInstance(any()) } returns watchDatabase

        scenario = launchActivity()
    }

    @After
    fun tearDown() {
        unmockkObject(WatchDatabase)
        watchDatabase.close()
    }

    @Test
    fun baseUICompletelyDisplayed() {
        onView(withId(R.id.finish_button)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.hint_text)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.toolbar_layout)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun uiShowsAllWatches() {
        // Check with multiple watches
        setWatchesInDatabase(listOf(dummyWatch1, dummyWatch2, dummyWatch3))
        onView(withId(R.id.available_watches_group)).check(
            matches(
                allOf(
                    hasChildCount(3),
                    withChild(withText(dummyWatch1.name)),
                    withChild(withText(dummyWatch2.name)),
                    withChild(withText(dummyWatch3.name))
                )
            )
        )
    }

    @Test
    fun uiShowsSingleWatch() {
        // Check with one watch
        setWatchesInDatabase(listOf(dummyWatch1))
        onView(withId(R.id.available_watches_group)).check(
            matches(
                allOf(
                    hasChildCount(1),
                    withChild(withText(dummyWatch1.name))
                )
            )
        )
    }

    @Test
    fun uiShowsNoWatches() {
        setWatchesInDatabase(emptyList())
        onView(withId(R.id.available_watches_group)).check(matches(hasChildCount(0)))
    }

    /**
     * Clear the database and add a list of [Watch] to it.
     */
    private fun setWatchesInDatabase(watches: List<Watch>) {
        watchDatabase.clearAllTables()
        watches.forEach {
            watchDatabase.watchDao().add(it)
        }
        // Wait for views to catch up
        Thread.sleep(300)
    }
}
