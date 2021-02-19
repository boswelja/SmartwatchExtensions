package com.boswelja.devicemanager.watchmanager.ui.register

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.viewModels
import com.boswelja.devicemanager.R
import org.junit.Before

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
}
