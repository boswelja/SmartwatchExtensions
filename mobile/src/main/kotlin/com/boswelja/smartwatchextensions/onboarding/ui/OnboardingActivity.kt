package com.boswelja.smartwatchextensions.onboarding.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import com.boswelja.smartwatchextensions.main.ui.MainActivity

/**
 * An Activity to handle user onboarding.
 */
class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            AppTheme {
                Scaffold(
                    topBar = {
                        UpNavigationAppBar(
                            onNavigateUp = {
                                if (!navController.navigateUp()) finish()
                            }
                        )
                    }
                ) {
                    OnboardingScreen(
                        modifier = Modifier.fillMaxSize().padding(it),
                        navController = navController,
                        onFinished = {
                            startActivity<MainActivity>()
                            finish()
                        }
                    )
                }
            }
        }
    }
}
