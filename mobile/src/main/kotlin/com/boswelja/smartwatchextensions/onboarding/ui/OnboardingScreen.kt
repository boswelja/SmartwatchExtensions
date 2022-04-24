package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
    onFinished: () -> Unit
) {
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
        composable(OnboardingDestination.REGISTER_WATCHES.route) {
            Box {
                var finishVisible by rememberSaveable {
                    mutableStateOf(false)
                }
                RegisterWatchScreen(
                    modifier = modifier.padding(bottom = 64.dp),
                    contentPadding = PaddingValues(contentPadding),
                    onWatchRegistered = {
                        finishVisible = true
                    }
                )
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = finishVisible,
                    enter = expandIn() + fadeIn(),
                    exit = shrinkOut() + fadeOut()
                ) {
                    ExtendedFloatingActionButton(
                        modifier = Modifier
                            .padding(contentPadding),
                        text = { Text(stringResource(R.string.button_finish)) },
                        icon = { Icon(Icons.Default.Check, null) },
                        onClick = onFinished
                    )
                }
            }
        }
    }
}

/**
 * All available navigation destinations for the onboarding flow.
 * @param route The navigation route.
 */
enum class OnboardingDestination(val route: String) {
    WELCOME("welcome"),
    REGISTER_WATCHES("register-watches")
}
