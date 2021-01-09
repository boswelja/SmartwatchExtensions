package com.boswelja.devicemanager.watchmanager.ui.register

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.boswelja.devicemanager.R
import org.junit.Before

class WatchRegisterResultsFragmentTest {

    private lateinit var fragmentScenario: FragmentScenario<WatchRegisterResultsFragment>

    @Before
    fun setUp() {
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_App)
    }

}