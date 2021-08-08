package com.boswelja.smartwatchextensions.phonelocking.ui

import android.content.Intent
import android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.SwitchSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun PhoneLockingSettingsScreen() {
    val context = LocalContext.current
    val viewModel: PhoneLockingSettingsViewModel = viewModel()
    val phoneLockingEnabled by viewModel.phoneLockingEnabled.collectAsState(false, Dispatchers.IO)
    var phoneLockingSetupVisible by remember { mutableStateOf(false) }
    Column {
        ListItem(
            icon = { Icon(Icons.Outlined.Settings, null) },
            text = {
                Text(stringResource(R.string.phone_locking_accessibility_settings))
            },
            modifier = Modifier.clickable {
                Timber.i("Opening accessibility settings")
                context.startActivity(Intent(ACTION_ACCESSIBILITY_SETTINGS))
            }
        )
        SwitchSetting(
            icon = { Icon(Icons.Outlined.PhonelinkLock, null) },
            label = { Text(stringResource(R.string.phone_locking_enabled_title)) },
            summary = { Text(stringResource(R.string.phone_locking_enabled_summary)) },
            checked = phoneLockingEnabled,
            onCheckChanged = {
                if (it) {
                    if (viewModel.canEnablePhoneLocking()) {
                        viewModel.setPhoneLockingEnabled(it)
                    } else {
                        phoneLockingSetupVisible = true
                    }
                } else {
                    viewModel.setPhoneLockingEnabled(it)
                }
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
