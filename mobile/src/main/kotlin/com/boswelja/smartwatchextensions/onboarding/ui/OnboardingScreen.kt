package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen to handle the onboarding flow.
 * @param modifier [Modifier].
 * @param navController [NavHostController].
 * @param onFinished Called when onboarding is complete.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onFinished: () -> Unit,
    onAbort: () -> Unit
) {
    val viewModel = getViewModel<OnboardingViewModel>()
    var compatibilityDialogVisible by remember {
        mutableStateOf(false)
    }
    NavHost(
        navController = navController,
        startDestination = OnboardingDestination.WELCOME.route
    ) {
        composable(OnboardingDestination.WELCOME.route) {
            WelcomeScreen(
                modifier = modifier,
                onNavigateNext = {
                    viewModel.checkSmartwatchesSupported {
                        if (it) {
                            navController.navigate(OnboardingDestination.SHARE_USAGE_STATS.route)
                        } else {
                            compatibilityDialogVisible = true
                        }
                    }
                }
            )
        }
        composable(OnboardingDestination.SHARE_USAGE_STATS.route) {
            AnalyticsScreen(
                modifier = modifier,
                onNavigateNext = {
                    viewModel.setAnalyticsEnabled(true)
                    navController.navigate(OnboardingDestination.REGISTER_WATCHES.route)
                },
                onOptOut = {
                    viewModel.setAnalyticsEnabled(false)
                    navController.navigate(OnboardingDestination.REGISTER_WATCHES.route)
                }
            )
        }
        composable(OnboardingDestination.REGISTER_WATCHES.route) {
            val availableWatches by viewModel.availableWatches()
                .collectAsState(initial = emptyList())
            RegisterWatchScreen(
                availableWatches,
                onWatchRegistered = onFinished,
                registerWatch = viewModel::registerWatch
            )
        }
    }
    CompatibilityDialog(
        visible = compatibilityDialogVisible,
        onDismissRequest = onAbort
    )
}

/**
 * All available navigation destinations for the onboarding flow.
 * @param route The navigation route.
 */
enum class OnboardingDestination(val route: String) {
    WELCOME("welcome"),
    SHARE_USAGE_STATS("usage-stats"),
    REGISTER_WATCHES("register-watches")
}
