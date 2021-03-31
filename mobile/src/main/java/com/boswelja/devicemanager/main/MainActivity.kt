package com.boswelja.devicemanager.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val watchManager = remember {
                WatchManager.getInstance(this)
            }
            val selectedWatch by watchManager.selectedWatch.observeAsState()
            val registeredWatches by watchManager.registeredWatches.observeAsState()

            val scaffoldState = rememberScaffoldState()
            val (currentDestination, setCurrentDestination) = remember {
                mutableStateOf(Destination.DASHBOARD)
            }

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
                    bottomBar = {
                        BottonNav(currentDestination, setCurrentDestination)
                    }
                ) {
                    MainScreen(
                        scaffoldState = scaffoldState,
                        currentDestination = currentDestination
                    )
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

    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @Composable
    fun MainScreen(scaffoldState: ScaffoldState, currentDestination: Destination) {
        Crossfade(targetState = currentDestination) {
            when (it) {
                Destination.DASHBOARD -> DashboardScreen()
                Destination.MESSAGES -> MessagesScreen(scaffoldState = scaffoldState)
                Destination.SETTINGS -> AppSettingsScreen()
                Destination.ABOUT -> AboutAppScreen()
            }
        }
    }

    @Composable
    fun BottonNav(currentDestination: Destination, setCurrentDestination: (Destination) -> Unit) {
        BottomNavigation(
            backgroundColor = MaterialTheme.colors.background
        ) {
            BottomNavItem(
                selected = currentDestination == Destination.DASHBOARD,
                icon = Icons.Outlined.Dashboard,
                label = stringResource(R.string.bottom_nav_dashboard_label),
                onClick = {
                    setCurrentDestination(Destination.DASHBOARD)
                }
            )
            BottomNavItem(
                selected = currentDestination == Destination.MESSAGES,
                icon = Icons.Outlined.Message,
                label = stringResource(R.string.nav_messages_label),
                onClick = {
                    setCurrentDestination(Destination.MESSAGES)
                }
            )
            BottomNavItem(
                selected = currentDestination == Destination.SETTINGS,
                icon = Icons.Outlined.Settings,
                label = stringResource(R.string.bottom_nav_app_settings_label),
                onClick = {
                    setCurrentDestination(Destination.SETTINGS)
                }
            )
            BottomNavItem(
                selected = currentDestination == Destination.ABOUT,
                icon = Icons.Outlined.Info,
                label = stringResource(R.string.bottom_nav_about_label),
                onClick = {
                    setCurrentDestination(Destination.ABOUT)
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

    enum class Destination {
        DASHBOARD,
        MESSAGES,
        SETTINGS,
        ABOUT
    }

    companion object {
        private const val LOW_PRIORITY_UPDATE = 2
        private const val HIGH_PRIORITY_UPDATE = 5
    }
}
