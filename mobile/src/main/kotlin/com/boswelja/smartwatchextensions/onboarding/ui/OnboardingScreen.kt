package com.boswelja.smartwatchextensions.onboarding.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
    navController: NavHostController,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = OnboardingDestination.WELCOME.route
    ) {
        composable(OnboardingDestination.WELCOME.route) {
            WelcomeScreen(
                modifier = modifier,
                contentPadding = contentPadding,
                onNavigateTo = { navController.navigate(it.route) }
            )
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
                },
                onNavigateTo = { navController.navigate(it.route) }
            )
        }
        composable(OnboardingDestination.REGISTER_WATCHES.route) {
            Box {
                RegisterWatchScreen(
                    modifier = modifier.padding(bottom = 64.dp),
                    contentPadding = contentPadding
                )
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(contentPadding).align(Alignment.BottomCenter),
                    text = { Text(stringResource(R.string.button_finish)) },
                    icon = { Icon(Icons.Outlined.Check, null) },
                    onClick = onFinished
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
