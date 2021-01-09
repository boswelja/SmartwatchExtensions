package com.boswelja.devicemanager.watchmanager.ui.register

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.boswelja.devicemanager.R
import org.junit.Before

class RegisterWatchFragmentTest {

    private lateinit var fragmentScenario: FragmentScenario<RegisterWatchFragment>

    @Before
    fun setUp() {
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_App)
    }

}