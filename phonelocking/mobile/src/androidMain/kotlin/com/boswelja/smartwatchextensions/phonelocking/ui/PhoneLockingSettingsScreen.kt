package com.boswelja.smartwatchextensions.phonelocking.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.phonelocking.R
import com.boswelja.smartwatchextensions.settings.ui.SwitchSetting
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen for displaying Phone Locking settings.
 * @param modifier [Modifier].
 * @param contentPadding The padding for the screen.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PhoneLockingSettingsScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp
) {
    val viewModel: PhoneLockingSettingsViewModel = getViewModel()
    val phoneLockingEnabled by viewModel.phoneLockingEnabled.collectAsState()
    var phoneLockingSetupVisible by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        ListItem(
            icon = { Icon(Icons.Outlined.Settings, null) },
            text = { Text(stringResource(R.string.phone_locking_accessibility_settings)) },
            modifier = Modifier.clickable {
                onNavigate(PhoneLockingDestination.ACCESSIBILITY_SETTINGS.route)
            }
        )
        SwitchSetting(
            icon = { Icon(Icons.Outlined.PhonelinkLock, null) },
            label = { Text(stringResource(R.string.phone_locking_enabled_title)) },
            summary = { Text(stringResource(R.string.phone_locking_enabled_summary)) },
            checked = phoneLockingEnabled,
            onCheckChanged = {
                phoneLockingSetupVisible = !viewModel.setPhoneLockingEnabled(it)
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
}
