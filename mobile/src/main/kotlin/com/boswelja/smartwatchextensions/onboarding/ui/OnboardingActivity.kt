package com.boswelja.smartwatchextensions.onboarding.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import com.boswelja.smartwatchextensions.main.MainActivity
import com.boswelja.smartwatchextensions.watchmanager.ui.register.RegisterWatchScreen
import com.boswelja.smartwatchextensions.watchmanager.ui.register.RegisterWatchViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

class OnboardingActivity : AppCompatActivity() {

    @ExperimentalCoroutinesApi
    private val registerWatchViewModel: RegisterWatchViewModel by viewModels()
    private val customTabIntent = CustomTabsIntent.Builder().setShowTitle(true).build()

    private var currentDestination by mutableStateOf(Destination.WELCOME)

    @ExperimentalCoroutinesApi
    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Scaffold(
                    topBar = {
                        UpNavigationAppBar(
                            onNavigateUp = { onBackPressed() }
                        )
                    },
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.button_next)) },
                            icon = { Icon(Icons.Outlined.NavigateNext, null) },
                            onClick = { navigateNext() }
                        )
                    }
                ) {
                    OnboardingScreen(currentDestination = currentDestination)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun navigateNext() {
        when (currentDestination) {
            Destination.WELCOME -> currentDestination = Destination.SHARE_USAGE_STATS
            Destination.SHARE_USAGE_STATS -> currentDestination = Destination.REGISTER_WATCHES
            Destination.REGISTER_WATCHES -> {
                startActivity<MainActivity>()
                finish()
            }
        }
    }

    @ExperimentalCoroutinesApi
    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @Composable
    fun OnboardingScreen(currentDestination: Destination) {
        Crossfade(targetState = currentDestination) {
            when (it) {
                Destination.WELCOME -> WelcomeScreen()
                Destination.SHARE_USAGE_STATS -> {
                    UsageStatsScreen(
                        onShowPrivacyPolicy = {
                            customTabIntent.launchUrl(
                                this@OnboardingActivity,
                                getString(R.string.privacy_policy_url).toUri()
                            )
                        }
                    )
                }
                Destination.REGISTER_WATCHES -> {
                    RegisterWatchScreen(registerWatchViewModel.addedWatches)
                }
            }
        }
    }

    enum class Destination {
        WELCOME,
        SHARE_USAGE_STATS,
        REGISTER_WATCHES
    }
}
