package com.boswelja.devicemanager.watchinfo.ui

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.connection.Capability
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.android.material.textfield.TextInputLayout
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import org.junit.Before
import org.junit.Test
import kotlin.experimental.or

class WatchInfoActivityTest {

    private val dummyWatch = Watch("watch-id", "Watch", "platform")
    private val registeredWatches = MutableLiveData(listOf(dummyWatch))

    private lateinit var scenario: ActivityScenario<WatchInfoActivity>

    @RelaxedMockK
    lateinit var watchManager: WatchManager

    @Before
    fun setUp() {
        dummyWatch.capabilities = Capability.MANAGE_APPS.id or Capability.SYNC_BATTERY.id
        MockKAnnotations.init(this)
        mockkObject(WatchManager)
        every { WatchManager.Companion.getInstance(any()) } returns watchManager
        every { watchManager.registeredWatches } returns registeredWatches

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            WatchInfoActivity::class.java
        )
        intent.putExtra(WatchInfoActivity.EXTRA_WATCH_ID, dummyWatch.id)

        scenario = launchActivity(intent)
    }

    @Test
    fun viewsVisible() {
        onView(withId(R.id.watch_name_layout)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.capabilities_recyclerview)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.clear_preferences_button)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.forget_watch_button)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun watchNameFieldFunctionsCorrectly() {
        // Check watch name is populated on create
        onView(withId(R.id.watch_name_field)).check(matches(withText(dummyWatch.name)))

        // Check empty text shows error
        onView(withId(R.id.watch_name_field)).perform(replaceText(""))
        onView(withId(R.id.watch_name_layout)).check { view, noViewFoundException ->
            if (view !is TextInputLayout) {
                throw noViewFoundException
            }
            assertThat(view.isErrorEnabled).isTrue()
        }

        // Check name updates in database on stop
        val newName = "new name"
        onView(withId(R.id.watch_name_field))
            .perform(replaceText(newName))
        scenario.moveToState(Lifecycle.State.DESTROYED)
        coVerify { watchManager.renameWatch(dummyWatch, newName) }
    }

    @Test
    fun capabilitiesListUpdatesCorrectly() {
        // Check capabilities listed on init
        val expectedCapabilities = Capability.values()
            .filter { dummyWatch.hasCapability(it) }.toMutableList()
        onView(withId(R.id.capabilities_recyclerview)).check { view, noViewFoundException ->
            if (view !is RecyclerView) {
                throw noViewFoundException
            }

            assertThat(view.adapter?.itemCount).isEqualTo(expectedCapabilities.count())
        }
        expectedCapabilities.forEach {
            onView(withId(R.id.capabilities_recyclerview))
                .check(matches(hasDescendant(withText(it.label))))
        }

        // Try changing watch capabilities
        expectedCapabilities.add(Capability.RECEIVE_DND)
        val dummyWatch2 = dummyWatch
        dummyWatch2.capabilities = dummyWatch2.capabilities or Capability.RECEIVE_DND.id
        scenario.onActivity {
            registeredWatches.value = listOf(dummyWatch2)
        }
        onView(withId(R.id.capabilities_recyclerview)).check { view, noViewFoundException ->
            if (view !is RecyclerView) {
                throw noViewFoundException
            }

            assertThat(view.adapter?.itemCount).isEqualTo(expectedCapabilities.count())
        }
        expectedCapabilities.forEach {
            onView(withId(R.id.capabilities_recyclerview))
                .check(matches(hasDescendant(withText(it.label))))
        }
    }

    @Test
    fun clearPreferencesButtonOpensSheet() {
        onView(withId(R.id.clear_preferences_button)).perform(click())
        scenario.onActivity {
            assertThat(
                it.supportFragmentManager
                    .findFragmentByTag(ClearWatchPreferencesSheet::class.simpleName)
            ).isNotNull()
        }
    }

    @Test
    fun forgetWatchButtonOpensSheet() {
        onView(withId(R.id.forget_watch_button)).perform(click())
        scenario.onActivity {
            assertThat(
                it.supportFragmentManager
                    .findFragmentByTag(ForgetWatchSheet::class.simpleName)
            ).isNotNull()
        }
    }
}
