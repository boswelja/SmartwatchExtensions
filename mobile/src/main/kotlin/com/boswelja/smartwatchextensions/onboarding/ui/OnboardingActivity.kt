package com.boswelja.smartwatchextensions.onboarding.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import com.boswelja.smartwatchextensions.watchmanager.ui.register.RegisterWatchScreen

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            AppTheme {
                Scaffold(
                    topBar = {
                        UpNavigationAppBar(onNavigateUp = navController::navigateUp)
                    },
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.button_next)) },
                            icon = { Icon(Icons.Outlined.NavigateNext, null) },
                            onClick = { /* TODO */ }
                        )
                    }
                ) {
                    OnboardingScreen(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController
                    )
                }
            }
        }
    }

    @Composable
    fun OnboardingScreen(
        modifier: Modifier = Modifier,
        contentPadding: Dp = 16.dp,
        navController: NavHostController
    ) {
        NavHost(
            navController = navController,
            startDestination = OnboardingDestination.WELCOME.route
        ) {
            composable(OnboardingDestination.WELCOME.route) {
                WelcomeScreen(modifier = modifier, contentPadding = contentPadding)
            }
            composable(OnboardingDestination.SHARE_USAGE_STATS.route) {
                UsageStatsScreen(
                    modifier = modifier,
                    contentPadding = contentPadding,
                    onShowPrivacyPolicy = {
                        startActivity { intent ->
                            intent.action = Intent.ACTION_VIEW
                            intent.data = getString(R.string.privacy_policy_url).toUri()
                            intent
                        }
                    }
                )
            }
            composable(OnboardingDestination.REGISTER_WATCHES.route) {
                RegisterWatchScreen(
                    modifier = modifier,
                    contentPadding = contentPadding
                )
            }
        }
    }
}

enum class OnboardingDestination(val route: String) {
    WELCOME("welcome"),
    SHARE_USAGE_STATS("usage-stats"),
    REGISTER_WATCHES("register-watches")
}
