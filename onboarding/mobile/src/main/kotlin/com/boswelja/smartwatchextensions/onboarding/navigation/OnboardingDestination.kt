package com.boswelja.smartwatchextensions.onboarding.navigation

/**
 * All available navigation destinations for the onboarding flow.
 * @param route The navigation route.
 */
enum class OnboardingDestination(val route: String) {
    WELCOME("welcome"),
    REGISTER_WATCHES("register-watches")
}
