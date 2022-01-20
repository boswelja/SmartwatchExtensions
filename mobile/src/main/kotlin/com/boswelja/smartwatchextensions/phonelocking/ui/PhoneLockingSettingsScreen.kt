package com.boswelja.smartwatchextensions.phonelocking.ui

import android.content.Intent
import android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import com.boswelja.smartwatchextensions.settings.ui.SwitchSetting
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen for displaying Phone Locking settings.
 * @param modifier [Modifier].
 * @param contentPadding The padding for the screen.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PhoneLockingSettingsScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        PhoneLockingSettingsCard()
    }
}

/**
 * A Composable for showing Phone Locking settings.
 * @param modifier [Modifier].
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PhoneLockingSettingsCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: PhoneLockingSettingsViewModel = getViewModel()
    val phoneLockingEnabled by viewModel.phoneLockingEnabled.collectAsState(false, Dispatchers.IO)
    var phoneLockingSetupVisible by remember { mutableStateOf(false) }
    Card(
        modifier = modifier,
        header = {
            CardHeader(
                title = { Text(stringResource(R.string.main_phone_locking_title)) }
            )
        }
    ) {
        Column {
            ListItem(
                icon = { Icon(Icons.Outlined.Settings, null) },
                text = { Text(stringResource(R.string.phone_locking_accessibility_settings)) },
                modifier = Modifier.clickable {
                    context.startActivity(Intent(ACTION_ACCESSIBILITY_SETTINGS))
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
                            content = { Text(stringResource(R.string.dialog_button_grant)) },
                            onClick = {
                                context.startActivity(Intent(ACTION_ACCESSIBILITY_SETTINGS))
                                phoneLockingSetupVisible = false
                            }
                        )
                    },
                    dismissButton = {
                        TextButton(
                            content = { Text(stringResource(R.string.dialog_button_cancel)) },
                            onClick = { phoneLockingSetupVisible = false }
                        )
                    },
                    onDismissRequest = { phoneLockingSetupVisible = false }
                )
            }
        }
    }
}
