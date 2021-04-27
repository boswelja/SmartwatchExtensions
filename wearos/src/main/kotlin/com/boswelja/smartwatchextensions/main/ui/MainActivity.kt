package com.boswelja.smartwatchextensions.main.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.common.AppTheme
import com.boswelja.smartwatchextensions.extensions.ui.ExtensionsScreen
import com.boswelja.smartwatchextensions.onboarding.ui.OnboardingScreen

class MainActivity : AppCompatActivity() {

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isRegistered by viewModel.isRegistered.observeAsState()
            AppTheme {
                if (isRegistered == true) {
                    ExtensionsScreen()
                } else {
                    OnboardingScreen()
                }
            }
        }
    }
}
