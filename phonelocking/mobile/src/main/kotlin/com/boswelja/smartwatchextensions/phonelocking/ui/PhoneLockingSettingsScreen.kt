package com.boswelja.smartwatchextensions.phonelocking.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.core.ui.settings.HeroSetting
import com.boswelja.smartwatchextensions.core.ui.settings.ShortcutSetting
import com.boswelja.smartwatchextensions.phonelocking.platform.PhoneLockingAccessibilityService
import com.boswelja.smartwatchextensions.phonelocking.R
import org.koin.compose.viewmodel.koinViewModel

/**
 * A Composable screen for displaying Phone Locking settings.
 * @param modifier [Modifier].
 */
@Composable
fun PhoneLockingSettingsScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: PhoneLockingSettingsViewModel = koinViewModel()
    val context = LocalContext.current

    val phoneLockingEnabled by viewModel.phoneLockingEnabled.collectAsState()

    Column(modifier) {
        PhoneLockingSetting(
            phoneLockingEnabled = phoneLockingEnabled,
            onPhoneLockingEnabledChanged = viewModel::setPhoneLockingEnabled,
            onCheckAccessibilityServiceEnabled = { PhoneLockingAccessibilityService.isEnabled(context) },
            onNavigate = onNavigate
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.phone_locking_accessibility_settings)) },
            onClick = { onNavigate(PhoneLockingDestination.ACCESSIBILITY_SETTINGS.route) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Displays a SwitchSetting to control the state of Phone Locking. If the Accessibility Service is disabled, the user
 * will be prompted to enable it first.
 * @param phoneLockingEnabled Whether Phone Locking is currently enabled.
 * @param onPhoneLockingEnabledChanged Called when the Phone Locking enabled state should change.
 * @param onCheckAccessibilityServiceEnabled Called when checking to see if the Accessibility Service is currently
 * running. Should return true if it is running, false otherwise.
 * @param onNavigate Called when navigation is requested.
 */
@Composable
fun PhoneLockingSetting(
    phoneLockingEnabled: Boolean,
    onPhoneLockingEnabledChanged: (Boolean) -> Unit,
    onCheckAccessibilityServiceEnabled: () -> Boolean,
    onNavigate: (String) -> Unit
) {
    var phoneLockingSetupVisible by rememberSaveable { mutableStateOf(false) }

    HeroSetting(
        checked = phoneLockingEnabled,
        onCheckedChange = {
            phoneLockingSetupVisible = it && !onCheckAccessibilityServiceEnabled()
            if (!phoneLockingSetupVisible) onPhoneLockingEnabledChanged(it)
        },
        text = { Text(stringResource(R.string.phone_locking_enabled_title)) }
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
