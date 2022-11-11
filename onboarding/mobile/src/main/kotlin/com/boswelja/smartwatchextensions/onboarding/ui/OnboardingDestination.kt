package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

fun NavGraphBuilder.onboardingGraph(
    graphRoute: String,
    onNavigate: (route: String) -> Unit,
    screenModifier: Modifier = Modifier
) = navigation(
    route = graphRoute,
    startDestination = OnboardingDestination.WELCOME.route
) {
    composable(OnboardingDestination.WELCOME.route) {
        WelcomeScreen(
            onNavigateTo = { onNavigate(it.route) },
            modifier = screenModifier
        )
    }
    composable(OnboardingDestination.REGISTER_WATCHES.route) {

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
