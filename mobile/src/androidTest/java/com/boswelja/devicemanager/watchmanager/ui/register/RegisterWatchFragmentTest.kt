package com.boswelja.devicemanager.watchmanager.ui.register

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.TestExtensions.getOrAwaitValue
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.connection.wearos.WearOSConnectionInterface
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test

class RegisterWatchFragmentTest {

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1", WearOSConnectionInterface.PLATFORM)
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2", WearOSConnectionInterface.PLATFORM)
    private val dummyWatch3 = Watch("an-id-3456", "Watch 3", WearOSConnectionInterface.PLATFORM)
    private val dummyWatches = listOf(dummyWatch1, dummyWatch2, dummyWatch3)

    @RelaxedMockK
    private lateinit var watchManager: WatchManager

    private lateinit var fragmentScenario: FragmentScenario<RegisterWatchFragment>
    private lateinit var availableWatches: MutableLiveData<List<Watch>>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        availableWatches = MutableLiveData()
        mockkObject(WatchManager.Companion)
        every { WatchManager.Companion.getInstance(any()) } returns watchManager
        every { watchManager.availableWatches } returns availableWatches

        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_App)
    }

    @Test
    fun viewsVisible() {
        onView(withId(R.id.title)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.desc)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.finish_button)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.progress_indicator)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun finishButtonFiresViewModelEvent() {
        onView(withId(R.id.finish_button)).perform(click())
        fragmentScenario.onFragment {
            assertThat(
                it.activityViewModels<RegisterWatchViewModel>().value.onFinished.getOrAwaitValue()
            ).isTrue()
        }
    }

    @Test
    fun noWatchesViewShownWhenNoWatches() {
        // Test with availableWatches empty
        fragmentScenario.onFragment {
            availableWatches.postValue(emptyList())
        }
        onView(withId(R.id.no_watches_text)).check(matches(isCompletelyDisplayed()))

        // Populate availableWatches and let fragment populate registeredWatches
        fragmentScenario.onFragment {
            availableWatches.postValue(dummyWatches)
            it.activityViewModels<RegisterWatchViewModel>().value
                .registeredWatches.getOrAwaitValue()
        }
        onView(withId(R.id.no_watches_text))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)))
    }

    @Test
    fun finishButtonDisabledWithNoRegisteredWatches() {
        // Test with availableWatches empty
        fragmentScenario.onFragment {
            availableWatches.postValue(emptyList())
        }
        onView(withId(R.id.finish_button)).check(matches(not(isEnabled())))

        // Populate availableWatches and let fragment populate registeredWatches
        fragmentScenario.onFragment {
            availableWatches.postValue(dummyWatches)
            it.activityViewModels<RegisterWatchViewModel>().value
                .registeredWatches.getOrAwaitValue()
        }
        onView(withId(R.id.finish_button)).check(matches(isEnabled()))
    }

    @Test
    fun availableWatchesGetRegistered() {
        // Test with availableWatches empty
        fragmentScenario.onFragment {
            availableWatches.postValue(emptyList())
        }
        fragmentScenario.onFragment {
            availableWatches.postValue(dummyWatches)
            assertThat(
                it.activityViewModels<RegisterWatchViewModel>().value
                    .registeredWatches.getOrAwaitValue()
            ).isEmpty()
        }

        // Populate availableWatches with one watch
        fragmentScenario.onFragment {
            availableWatches.postValue(listOf(dummyWatch1))
            assertThat(
                it.activityViewModels<RegisterWatchViewModel>().value
                    .registeredWatches.getOrAwaitValue()
            ).containsExactly(dummyWatch1)
        }

        // Populate availableWatches with more watches
        fragmentScenario.onFragment {
            availableWatches.postValue(dummyWatches)
            assertThat(
                it.activityViewModels<RegisterWatchViewModel>().value
                    .registeredWatches.getOrAwaitValue()
            ).containsExactlyElementsIn(dummyWatches)
        }
    }
}
