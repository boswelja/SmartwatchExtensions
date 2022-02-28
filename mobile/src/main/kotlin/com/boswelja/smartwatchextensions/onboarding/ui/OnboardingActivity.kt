package com.boswelja.smartwatchextensions.onboarding.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.core.ui.theme.HarmonizedTheme
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

            HarmonizedTheme {
                Scaffold(
                    topBar = {
                        MediumTopAppBar(
                            title = { },
                            navigationIcon = {
                                IconButton(onClick = this::finish) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                ) {
                    OnboardingScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
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
