package com.boswelja.smartwatchextensions.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.extensions.ui.ExtensionsScreen
import com.boswelja.smartwatchextensions.onboarding.ui.OnboardingScreen
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainScreen(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: MainViewModel = viewModel()
    val isRegistered by viewModel.isRegistered.collectAsState(true, Dispatchers.IO)

    if (isRegistered) {
        ExtensionsScreen()
    } else {
        OnboardingScreen()
    }
}
