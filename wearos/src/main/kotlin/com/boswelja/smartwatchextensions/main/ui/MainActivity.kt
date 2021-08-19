package com.boswelja.smartwatchextensions.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.extensions.ui.ExtensionsScreen
import com.boswelja.smartwatchextensions.onboarding.ui.OnboardingScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainActivity : ComponentActivity() {

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isRegistered by viewModel.isRegistered.collectAsState(true, Dispatchers.IO)
            AppTheme {
                if (isRegistered) {
                    ExtensionsScreen()
                } else {
                    OnboardingScreen()
                }
            }
        }
    }
}
