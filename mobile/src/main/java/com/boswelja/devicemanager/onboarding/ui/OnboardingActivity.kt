package com.boswelja.devicemanager.onboarding.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.KEY_ROUTE
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.LifecycleAwareTimer
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.UpNavigationAppBar
import com.boswelja.devicemanager.main.MainActivity
import com.boswelja.devicemanager.watchmanager.ui.register.RegisterWatchViewModel
import com.boswelja.devicemanager.watchmanager.ui.register.RegisterWatchesScreen

class OnboardingActivity : AppCompatActivity() {

    private val registerWatchViewModel: RegisterWatchViewModel by viewModels()
    private val customTabIntent = CustomTabsIntent.Builder().setShowTitle(true).build()
    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    private val availableWatchUpdateTimer = LifecycleAwareTimer(TIMER_UPDATE_SECONDS) {
        registerWatchViewModel.refreshData()
    }

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                val navController = rememberNavController()
                val registeredWatches by registerWatchViewModel.registeredWatches.observeAsState()
                Scaffold(
                    topBar = {
                        UpNavigationAppBar(
                            onNavigateUp = {
                                if (!navController.popBackStack()) finish()
                            }
                        )
                    },
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.button_next)) },
                            icon = { Icon(Icons.Outlined.NavigateNext, null) },
                            onClick = {
                                navigateNext(navController)
                            }
                        )
                    }
                ) {
                    NavHost(navController = navController, "welcome") {
                        composable("welcome") { WelcomeScreen() }
                        composable("analytics") {
                            UsageStatsScreen(
                                sharedPreferences = sharedPreferences,
                                onShowPrivacyPolicy = {
                                    customTabIntent.launchUrl(
                                        this@OnboardingActivity,
                                        getString(R.string.privacy_policy_url).toUri()
                                    )
                                }
                            )
                        }
                        composable("registerWatches") {
                            RegisterWatchesScreen(registeredWatches = registeredWatches)
                        }
                    }
                }
            }
        }

        lifecycle.addObserver(availableWatchUpdateTimer)

        registerWatchViewModel.availableWatches.observe(this) {
            it.forEach { watch ->
                registerWatchViewModel.registerWatch(watch)
            }
        }
    }

    private fun navigateNext(navController: NavHostController) {
        when (navController.currentBackStackEntry?.arguments?.getString(KEY_ROUTE)) {
            "welcome" -> navController.navigate("analytics")
            "analytics" -> navController.navigate("registerWatches")
            "registerWatches" -> {
                if (!registerWatchViewModel.registeredWatches.value.isNullOrEmpty()) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            else -> navController.navigate("welcome")
        }
    }

    companion object {
        private const val TIMER_UPDATE_SECONDS: Long = 5
    }
}
