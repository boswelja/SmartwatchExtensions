package com.boswelja.devicemanager.watchmanager.ui.register

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.viewModels
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.boswelja.devicemanager.EmptyParentFragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.TestExtensions.getOrAwaitValue
import com.boswelja.devicemanager.TestExtensions.hasPlural
import com.boswelja.devicemanager.TestExtensions.hasText
import com.boswelja.devicemanager.TestExtensions.withDrawable
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class WatchRegisterResultsFragmentTest {

    private val dummyWatch1 = Watch("an-id-1234", "Watch 1")
    private val dummyWatch2 = Watch("an-id-2345", "Watch 2")
    private val dummyWatch3 = Watch("an-id-3456", "Watch 3")
    private val dummyWatches = listOf(dummyWatch1, dummyWatch2, dummyWatch3)

    private lateinit var fragmentScenario: FragmentScenario<EmptyParentFragment>
    private lateinit var fragment: WatchRegisterResultsFragment
    private lateinit var viewModel: RegisterWatchViewModel

    @Before
    fun setUp() {
        fragment = WatchRegisterResultsFragment()
        // Inject mock ViewModel into ViewModelStore
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_App)
        fragmentScenario.onFragment {
            viewModel = it.viewModels<RegisterWatchViewModel>().value
            it.childFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .commitNow()
        }
    }

    @Test
    fun watchesRegisteredUpdatesViews() {
        // Wait for ViewModel to finish before updating views
        fragmentScenario.onFragment {
            viewModel.isWorking.getOrAwaitValue(time = 5) { assertThat(it).isFalse() }
            fragment.showWatchesRegistered(dummyWatches)
        }
        onView(withId(R.id.status_indicator))
            .check(matches(withDrawable(R.drawable.wizard_ic_success)))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.status_title))
            .check(matches(hasPlural(R.plurals.register_watch_success_title, dummyWatches.count())))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.status_text))
            .check(matches(hasText(R.string.register_watch_success_info)))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.finish_button)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.try_again_button))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun watchRegisteredUpdatesViews() {
        // Wait for ViewModel to finish before updating views
        fragmentScenario.onFragment {
            viewModel.isWorking.getOrAwaitValue(time = 5) { assertThat(it).isFalse() }
            fragment.showWatchesRegistered(listOf(dummyWatch1))
        }
        onView(withId(R.id.status_indicator))
            .check(matches(withDrawable(R.drawable.wizard_ic_success)))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.status_title))
            .check(matches(hasPlural(R.plurals.register_watch_success_title, 1)))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.status_text))
            .check(matches(hasText(R.string.register_watch_success_info)))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.finish_button)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.try_again_button))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun noWatchesRegisteredUpdatesViews() {
        // Wait for ViewModel to finish before updating views
        fragmentScenario.onFragment {
            viewModel.isWorking.getOrAwaitValue(time = 5) { assertThat(it).isFalse() }
            fragment.showNoChanges()
        }
        onView(withId(R.id.status_indicator))
            .check(matches(withDrawable(R.drawable.wizard_ic_warning)))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.status_title))
            .check(matches(hasText(R.string.register_watch_no_watches_title)))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.status_text))
            .check(matches(hasText(R.string.register_watch_no_watches_info)))
            .check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.finish_button)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.try_again_button))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
            .check(matches(isCompletelyDisplayed()))
    }
}
