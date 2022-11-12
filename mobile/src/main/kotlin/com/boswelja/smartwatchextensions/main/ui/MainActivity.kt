package com.boswelja.smartwatchextensions.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The main app entry point.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        if (intent?.hasExtra(EXTRA_WATCH_ID) == true) {
            viewModel.selectWatchById(intent.getStringExtra(EXTRA_WATCH_ID)!!)
        }

        setContent {
            MainScreen()
        }
    }

    companion object {

        /**
         * The watch ID to select on launch.
         */
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}
