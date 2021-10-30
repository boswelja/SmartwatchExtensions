package com.boswelja.smartwatchextensions.onboarding.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.AppTheme3
import com.boswelja.smartwatchextensions.main.ui.MainActivity

/**
 * An Activity to handle user onboarding.
 */
class OnboardingActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            AppTheme3 {
                Scaffold {
                    OnboardingScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .padding(16.dp),
                        navController = navController,
                        onFinished = {
                            startActivity<MainActivity>()
                            finish()
                        },
                        onAbort = { finish() }
                    )
                }
            }
        }
    }
}
