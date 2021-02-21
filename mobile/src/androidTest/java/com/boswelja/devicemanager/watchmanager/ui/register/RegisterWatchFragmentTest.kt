package com.boswelja.devicemanager.watchmanager.ui.register

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import com.boswelja.devicemanager.watchmanager.connection.wearos.WearOSConnectionInterface
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.verify
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegisterWatchFragmentTest {

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1", WearOSConnectionInterface.PLATFORM)
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2", WearOSConnectionInterface.PLATFORM)
    private val dummyWatch3 = Watch("an-id-3456", "Watch 3", WearOSConnectionInterface.PLATFORM)
    private val dummyWatches = listOf(dummyWatch1, dummyWatch2, dummyWatch3)

    private lateinit var fragmentScenario: FragmentScenario<RegisterWatchFragment>
    private lateinit var availableWatches: MutableLiveData<List<Watch>>
    private lateinit var registeredWatches: MutableLiveData<List<Watch>>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        availableWatches = MutableLiveData()
        registeredWatches = MutableLiveData()

        mockkConstructor(RegisterWatchViewModel::class)
        every {
            anyConstructed<RegisterWatchViewModel>().availableWatches
        } returns availableWatches
        every {
            anyConstructed<RegisterWatchViewModel>().registeredWatches
        } returns registeredWatches
        every {
            anyConstructed<RegisterWatchViewModel>().registerWatch(any())
        } answers { }
        every {
            anyConstructed<RegisterWatchViewModel>().refreshData()
        } answers { }

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
        // Need to register watches to enable the button
        fragmentScenario.onFragment {
            registeredWatches.postValue(dummyWatches)
        }

        onView(withId(R.id.finish_button)).perform(click())
        fragmentScenario.onFragment {
            assertThat(
                it.activityViewModels<RegisterWatchViewModel>().value.onFinished.getOrAwaitValue()
            ).isTrue()
        }
    }

    @Test
    fun noWatchesViewShownWhenNoWatchesRegistered() {
        // Test with registeredWatches empty
        fragmentScenario.onFragment {
            registeredWatches.postValue(emptyList())
        }
        onView(withId(R.id.no_watches_text)).check(matches(isCompletelyDisplayed()))

        // Populate registeredWatches
        fragmentScenario.onFragment {
            registeredWatches.postValue(dummyWatches)
        }

        onView(withId(R.id.no_watches_text))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun finishButtonEnabledWithRegisteredWatches() {
        // Test with registeredWatches empty
        fragmentScenario.onFragment {
            registeredWatches.postValue(emptyList())
        }
        onView(withId(R.id.finish_button)).check(matches(not(isEnabled())))

        // Populate registeredWatches and let fragment populate registeredWatches
        fragmentScenario.onFragment {
            registeredWatches.postValue(dummyWatches)
        }
        onView(withId(R.id.finish_button)).check(matches(isEnabled()))
    }

    @Test
    fun availableWatchesAreRegistered() {
        // Test with availableWatches empty
        fragmentScenario.onFragment {
            availableWatches.postValue(emptyList())
        }
        verify(inverse = true) { anyConstructed<RegisterWatchViewModel>().registerWatch(any()) }

        // Populate availableWatches with one watch
        fragmentScenario.onFragment {
            availableWatches.postValue(listOf(dummyWatch1))
        }
        verify { anyConstructed<RegisterWatchViewModel>().registerWatch(dummyWatch1) }

        // Populate availableWatches with more watches
        fragmentScenario.onFragment {
            availableWatches.postValue(dummyWatches)
        }
        dummyWatches.forEach { watch ->
            verify { anyConstructed<RegisterWatchViewModel>().registerWatch(watch) }
        }
    }
}
