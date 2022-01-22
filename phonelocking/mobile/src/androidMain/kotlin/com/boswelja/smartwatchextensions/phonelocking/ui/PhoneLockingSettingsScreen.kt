package com.boswelja.smartwatchextensions.phonelocking.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhonelinkLock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.phonelocking.R
import com.boswelja.smartwatchextensions.settings.ui.SwitchSetting
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen for displaying Phone Locking settings.
 * @param modifier [Modifier].
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PhoneLockingSettingsScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: PhoneLockingSettingsViewModel = getViewModel()

    val watchName by viewModel.watchName.collectAsState()
    val phoneLockingEnabled by viewModel.phoneLockingEnabled.collectAsState()

    Column(modifier) {
        ListItem(
            icon = { Icon(Icons.Outlined.Settings, null) },
            text = { Text(stringResource(R.string.phone_locking_accessibility_settings)) },
            modifier = Modifier.clickable {
                onNavigate(PhoneLockingDestination.ACCESSIBILITY_SETTINGS.route)
            }
        )
        PhoneLockingSetting(
            watchName = watchName,
            phoneLockingEnabled = phoneLockingEnabled,
            onPhoneLockingEnabledChanged = viewModel::setPhoneLockingEnabled,
            onCheckAccessibilityServiceEnabled = viewModel::canEnablePhoneLocking,
            onNavigate = onNavigate
        )
    }
}

/**
 * Displays a SwitchSetting to control the state of Phone Locking. If the Accessibility Service is disabled, the user
 * will be prompted to enable it first.
 * @param watchName The name of the currently selected watch.
 * @param phoneLockingEnabled Whether Phone Locking is currently enabled.
 * @param onPhoneLockingEnabledChanged Called when the Phone Locking enabled state should change.
 * @param onCheckAccessibilityServiceEnabled Called when checking to see if the Accessibility Service is currently
 * running. Should return true if it is running, false otherwise.
 * @param onNavigate Called when navigation is requested.
 */
@Composable
fun PhoneLockingSetting(
    watchName: String,
    phoneLockingEnabled: Boolean,
    onPhoneLockingEnabledChanged: (Boolean) -> Unit,
    onCheckAccessibilityServiceEnabled: () -> Boolean,
    onNavigate: (String) -> Unit
) {
    var phoneLockingSetupVisible by rememberSaveable { mutableStateOf(false) }

    SwitchSetting(
        icon = { Icon(Icons.Outlined.PhonelinkLock, null) },
        label = { Text(stringResource(R.string.phone_locking_enabled_title)) },
        summary = { Text(stringResource(R.string.phone_locking_enabled_summary, watchName)) },
        checked = phoneLockingEnabled,
        onCheckChanged = {
            phoneLockingSetupVisible = it && !onCheckAccessibilityServiceEnabled()
            if (!phoneLockingSetupVisible) onPhoneLockingEnabledChanged(it)
        }
    )
    if (phoneLockingSetupVisible) {
        AlertDialog(
            title = {
                Text(stringResource(R.string.phone_locking_setup_accessibility_title))
            },
            text = {
                Text(stringResource(R.string.phone_locking_setup_accessibility_desc))
            },
            confirmButton = {
                TextButton(
                    content = { Text(stringResource(R.string.phone_locking_setup_open_settings)) },
                    onClick = {
                        onNavigate(PhoneLockingDestination.ACCESSIBILITY_SETTINGS.route)
                        phoneLockingSetupVisible = false
                    }
                )
            },
            dismissButton = {
                TextButton(
                    content = { Text(stringResource(R.string.phone_locking_setup_cancel)) },
                    onClick = { phoneLockingSetupVisible = false }
                )
            },
            onDismissRequest = { phoneLockingSetupVisible = false }
        )
    }
}
