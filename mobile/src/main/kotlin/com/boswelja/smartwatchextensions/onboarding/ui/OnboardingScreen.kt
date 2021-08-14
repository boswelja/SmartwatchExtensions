package com.boswelja.smartwatchextensions.onboarding.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.watchmanager.ui.register.RegisterWatchScreen

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    navController: NavHostController
) {
    val context = LocalContext.current
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
                    context.startActivity { intent ->
                        intent.action = Intent.ACTION_VIEW
                        intent.data = context.getString(R.string.privacy_policy_url).toUri()
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

enum class OnboardingDestination(val route: String) {
    WELCOME("welcome"),
    SHARE_USAGE_STATS("usage-stats"),
    REGISTER_WATCHES("register-watches")
}
