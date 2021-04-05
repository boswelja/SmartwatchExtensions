package com.boswelja.devicemanager.phonelocking.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.DialogPreference
import com.boswelja.devicemanager.common.ui.SwitchPreference
import com.boswelja.devicemanager.phonelocking.DeviceAdminChangeReceiver
import com.boswelja.devicemanager.phonelocking.ui.PhoneLockingSettingsViewModel.Companion.PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE
import timber.log.Timber

@ExperimentalMaterialApi
@Composable
fun PhoneLockingSettingsScreen() {
    val context = LocalContext.current
    val viewModel: PhoneLockingSettingsViewModel = viewModel()
    val phoneLockingEnabled by viewModel.phoneLockingEnabled.observeAsState()
    val phoneLockingMode by viewModel.phoneLockingMode.observeAsState()
    val currentMode = viewModel.phoneLockingModes.first { it.second == phoneLockingMode }
    var phoneLockingSetupVisible by remember { mutableStateOf(false) }
    Column {
        DialogPreference(
            text = stringResource(R.string.phone_locking_mode_title),
            secondaryText = currentMode.first,
            values = viewModel.phoneLockingModes,
            value = currentMode,
            onValueChanged = {
                viewModel.switchMode(it.second)
            }
        )
        ListItem(
            icon = { Icon(Icons.Outlined.Settings, null) },
            text = {
                if (phoneLockingMode == PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE) {
                    Text(stringResource(R.string.phone_locking_accessibility_settings))
                } else {
                    Text(stringResource(R.string.phone_locking_admin_settings))
                }
            },
            modifier = Modifier.clickable {
                if (phoneLockingMode == PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE) {
                    Timber.i("Opening accessibility settings")
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } else {
                    Timber.i("Opening device admin settings")
                    Intent().apply {
                        ComponentName(
                            "com.android.settings", "com.android.settings.DeviceAdminSettings"
                        ).also { component = it }
                    }.also { context.startActivity(it) }
                }
            }
        )
        SwitchPreference(
            icon = Icons.Outlined.PhonelinkLock,
            text = stringResource(R.string.phone_locking_enabled_title),
            secondaryText = stringResource(R.string.phone_locking_enabled_summary),
            isChecked = phoneLockingEnabled == true,
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
                    if (phoneLockingMode == PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE) {
                        Text(stringResource(R.string.phone_locking_setup_accessibility_title))
                    } else {
                        Text(stringResource(R.string.phone_locking_setup_admin_title))
                    }
                },
                text = {
                    if (phoneLockingMode == PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE) {
                        Text(stringResource(R.string.phone_locking_setup_accessibility_desc))
                    } else {
                        Text(stringResource(R.string.phone_locking_setup_admin_desc))
                    }
                },
                confirmButton = {
                    TextButton(
                        content = { Text(stringResource(R.string.dialog_button_grant)) },
                        onClick = {
                            if (phoneLockingMode == PHONE_LOCKING_MODE_ACCESSIBILITY_SERVICE) {
                                context
                                    .startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            } else {
                                val intent =
                                    Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                        putExtra(
                                            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                            DeviceAdminChangeReceiver().getWho(context)
                                        )
                                        putExtra(
                                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                            context.getString(R.string.device_admin_desc)
                                        )
                                    }
                                context.startActivity(intent)
                            }
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
