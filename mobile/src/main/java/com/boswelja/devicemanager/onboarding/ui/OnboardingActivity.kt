package com.boswelja.devicemanager.onboarding.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.fragment.app.commit
import androidx.navigation.NavHostController
import androidx.navigation.compose.KEY_ROUTE
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.UpNavigationAppBar
import com.boswelja.devicemanager.main.MainActivity
import com.boswelja.devicemanager.watchmanager.ui.register.RegisterWatchFragment
import com.boswelja.devicemanager.watchmanager.ui.register.RegisterWatchViewModel

class OnboardingActivity : AppCompatActivity() {

    private val registerWatchViewModel: RegisterWatchViewModel by viewModels()
    private val customTabIntent = CustomTabsIntent.Builder().setShowTitle(true).build()
    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
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
                            AndroidView(
                                factory = {
                                    val view = FrameLayout(it)
                                    view.layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    view.id = R.id.fragment_holder
                                    supportFragmentManager.commit {
                                        replace(R.id.fragment_holder, RegisterWatchFragment())
                                    }
                                    view
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
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
}
