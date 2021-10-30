package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.devicemanagement.ui.register.RegisterWatchScreen
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen to handle the onboarding flow.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 * @param navController [NavHostController].
 * @param onFinished Called when onboarding is complete.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    navController: NavHostController,
    onFinished: () -> Unit,
    onAbort: () -> Unit
) {
    val viewModel = getViewModel<OnboardingViewModel>()
    var compatibilityDialogVisible by remember {
        mutableStateOf(false)
    }
    var isFinished by rememberSaveable {
        mutableStateOf(false)
    }
    NavHost(
        navController = navController,
        startDestination = OnboardingDestination.WELCOME.route
    ) {
        composable(OnboardingDestination.WELCOME.route) {
            WelcomeScreen(
                modifier = modifier.padding(contentPadding),
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
                modifier = modifier.padding(contentPadding),
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
            Box {
                RegisterWatchScreen(
                    modifier = modifier.padding(bottom = 64.dp).padding(contentPadding),
                    onWatchRegistered = {
                        isFinished = true
                    }
                )
                FinishButton(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = isFinished,
                    onClick = onFinished
                )
            }
        }
    }
    CompatibilityDialog(
        visible = compatibilityDialogVisible,
        onDismissRequest = onAbort
    )
}

/**
 * Displays the finish button to the user if [visible]. The button will be animated in/out as
 * needed.
 * @param modifier [Modifier].
 * @param visible Whether the button is visible.
 * @param onClick Called when the button is clicked.
 */
@Composable
fun FinishButton(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = expandIn() + fadeIn(),
        exit = shrinkOut() + fadeOut()
    ) {
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.button_finish)) },
            icon = { Icon(Icons.Outlined.Check, null) },
            onClick = onClick
        )
    }
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
