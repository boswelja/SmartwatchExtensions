package com.boswelja.devicemanager.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.KEY_ROUTE
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigate
import androidx.navigation.compose.rememberNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.aboutapp.ui.AboutAppScreen
import com.boswelja.devicemanager.appsettings.ui.AppSettingsScreen
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.WatchPickerAppBar
import com.boswelja.devicemanager.dashboard.ui.DashboardScreen
import com.boswelja.devicemanager.messages.Message
import com.boswelja.devicemanager.messages.MessageHandler
import com.boswelja.devicemanager.messages.Priority
import com.boswelja.devicemanager.messages.ui.MessagesScreen
import com.boswelja.devicemanager.onboarding.ui.OnboardingActivity
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.updatePriority
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val watchManager = remember {
                WatchManager.getInstance(this)
            }
            val selectedWatch by watchManager.selectedWatch.observeAsState()
            val registeredWatches by watchManager.registeredWatches.observeAsState()

            val navController = rememberNavController()
            val scaffoldState = rememberScaffoldState()

            AppTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        WatchPickerAppBar(
                            selectedWatch = selectedWatch,
                            watches = registeredWatches,
                            onWatchSelected = { watchManager.selectWatchById(it.id) }
                        )
                    },
                    bottomBar = { BottonNav(navController = navController) }
                ) {
                    NavHost(navController = navController, startDestination = ROUTE_DASHBOARD) {
                        composable(ROUTE_DASHBOARD) { DashboardScreen() }
                        composable(ROUTE_MESSAGES) { MessagesScreen(scaffoldState = scaffoldState) }
                        composable(ROUTE_SETTINGS) { AppSettingsScreen() }
                        composable(ROUTE_ABOUT) { AboutAppScreen() }
                    }
                }
            }
            if (registeredWatches != null && registeredWatches!!.isEmpty()) {
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
        }

        ensureAppUpdated()
    }

    private fun ensureAppUpdated() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.appUpdateInfo.addOnCompleteListener {
            if (it.isSuccessful) {
                val appUpdateInfo = it.result
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.updatePriority <= LOW_PRIORITY_UPDATE
                ) {
                    if (appUpdateInfo.updatePriority < HIGH_PRIORITY_UPDATE &&
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    ) {
                        val message = Message(
                            Message.Icon.UPDATE,
                            getString(R.string.update_available_title),
                            getString(R.string.update_available_text)
                        )
                        MessageHandler.postMessage(this, message, Priority.LOW)
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        appUpdateManager.startUpdateFlow(
                            appUpdateInfo,
                            this,
                            AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                        )
                    }
                }
            } else {
                Timber.w("Failed to check for app updates")
            }
        }
    }

    @Composable
    fun BottonNav(navController: NavHostController) {
        BottomNavigation(
            backgroundColor = MaterialTheme.colors.background
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.arguments?.getString(KEY_ROUTE)
            BottomNavItem(
                selected = currentRoute == ROUTE_DASHBOARD,
                icon = Icons.Outlined.Dashboard,
                label = stringResource(R.string.bottom_nav_dashboard_label),
                onClick = {
                    navController.navigate(ROUTE_DASHBOARD) {
                        popUpTo = navController.graph.startDestination
                        launchSingleTop = true
                    }
                }
            )
            BottomNavItem(
                selected = currentRoute == ROUTE_MESSAGES,
                icon = Icons.Outlined.Message,
                label = stringResource(R.string.nav_messages_label),
                onClick = {
                    navController.navigate(ROUTE_MESSAGES) {
                        popUpTo = navController.graph.startDestination
                        launchSingleTop = true
                    }
                }
            )
            BottomNavItem(
                selected = currentRoute == ROUTE_SETTINGS,
                icon = Icons.Outlined.Settings,
                label = stringResource(R.string.bottom_nav_app_settings_label),
                onClick = {
                    navController.navigate(ROUTE_SETTINGS) {
                        popUpTo = navController.graph.startDestination
                        launchSingleTop = true
                    }
                }
            )
            BottomNavItem(
                selected = currentRoute == ROUTE_ABOUT,
                icon = Icons.Outlined.Info,
                label = stringResource(R.string.bottom_nav_about_label),
                onClick = {
                    navController.navigate(ROUTE_ABOUT) {
                        popUpTo = navController.graph.startDestination
                        launchSingleTop = true
                    }
                }
            )
        }
    }

    @Composable
    fun RowScope.BottomNavItem(
        icon: ImageVector,
        label: String,
        selected: Boolean,
        onClick: () -> Unit
    ) {
        BottomNavigationItem(
            selected = selected,
            icon = { Icon(icon, null) },
            label = { Text(label) },
            onClick = onClick,
            alwaysShowLabel = false,
            selectedContentColor = MaterialTheme.colors.primary,
            unselectedContentColor = MaterialTheme.colors.onSurface
                .copy(alpha = ContentAlpha.medium)
        )
    }

    companion object {
        private const val ROUTE_DASHBOARD = "dashboard"
        private const val ROUTE_MESSAGES = "messages"
        private const val ROUTE_SETTINGS = "settings"
        private const val ROUTE_ABOUT = "about"

        private const val LOW_PRIORITY_UPDATE = 2
        private const val HIGH_PRIORITY_UPDATE = 5
    }
}
