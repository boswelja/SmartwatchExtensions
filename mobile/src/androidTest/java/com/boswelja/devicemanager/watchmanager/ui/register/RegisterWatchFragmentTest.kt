package com.boswelja.devicemanager.watchmanager.ui.register

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.viewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.TestExtensions.getOrAwaitValue
import com.boswelja.devicemanager.common.ui.LoadingFragment
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RegisterWatchFragmentTest {

    private lateinit var fragmentScenario: FragmentScenario<RegisterWatchFragment>
    private lateinit var viewModel: RegisterWatchViewModel

    @Before
    fun setUp() {
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_App)
        fragmentScenario.onFragment {
            viewModel = it.viewModels<RegisterWatchViewModel>().value
        }
    }

    @Test
    fun loadingShowsLoadingFragment() {
        fragmentScenario.onFragment {
            // Wait for ViewModel to finish working, then force transactions to execute
            viewModel.isWorking.getOrAwaitValue(time = 5) { isWorking -> assertThat(isWorking).isFalse() }
            it.childFragmentManager.executePendingTransactions()
            // Set new state, then force transactions to execute
            it.setLoading(true)
            it.childFragmentManager.executePendingTransactions()
            // Check the correct fragment is shown
            assertThat(it.childFragmentManager.findFragmentById(R.id.fragment_holder)).isInstanceOf(LoadingFragment::class.java)
        }
    }

    @Test
    fun noLoadingShowsResultFragment() {
        fragmentScenario.onFragment {
            // Wait for ViewModel to finish working, then force transactions to execute
            viewModel.isWorking.getOrAwaitValue(time = 5) { isWorking -> assertThat(isWorking).isFalse() }
            it.childFragmentManager.executePendingTransactions()
            // Set new state, then force transactions to execute
            it.setLoading(false)
            it.childFragmentManager.executePendingTransactions()
            // Check the correct fragment is shown
            assertThat(it.childFragmentManager.findFragmentById(R.id.fragment_holder)).isInstanceOf(WatchRegisterResultsFragment::class.java)
        }
    }
}