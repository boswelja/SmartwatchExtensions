package com.boswelja.smartwatchextensions.dndsync.ui

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.core.ui.settings.CheckboxSetting
import com.boswelja.smartwatchextensions.dndsync.DnDSyncSettingKeys.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.dndsync.DnDSyncSettingKeys.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.smartwatchextensions.dndsync.R
import kotlinx.coroutines.Dispatchers
import org.koin.compose.viewmodel.koinViewModel

/**
 * A Composable screen for displaying DnD Sync settings.
 * @param modifier [Modifier].
 */
@Composable
fun DnDSyncSettingsScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: DnDSyncSettingsViewModel = koinViewModel()
    var changingKey = rememberSaveable { "" }

    val canSendDnD by viewModel.canSendDnD.collectAsState(false, Dispatchers.IO)

    val syncToPhone by viewModel.syncToPhone.collectAsState(false, Dispatchers.IO)
    val syncWithTheater by viewModel.syncWithTheater.collectAsState(false, Dispatchers.IO)

    val notiPolicyAccessLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (viewModel.hasNotificationPolicyAccess) {
            when (changingKey) {
                DND_SYNC_TO_PHONE_KEY -> viewModel.setSyncToPhone(true)
                DND_SYNC_WITH_THEATER_KEY -> viewModel.setSyncWithTheater(true)
            }
        }
    }
    val onRequestNotiPolicyAccess = { key: String ->
        changingKey = key
        notiPolicyAccessLauncher
            .launch(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }

    Column(modifier) {
        SyncToPhoneSetting(
            enabled = canSendDnD,
            checked = syncToPhone,
            onCheckChanged = {
                if (it && !viewModel.hasNotificationPolicyAccess) {
                    onRequestNotiPolicyAccess(DND_SYNC_TO_PHONE_KEY)
                } else viewModel.setSyncToPhone(it)
            }
        )
        SyncWithTheaterSetting(
            enabled = canSendDnD,
            checked = syncWithTheater,
            onCheckChanged = {
                if (it && !viewModel.hasNotificationPolicyAccess) {
                    onRequestNotiPolicyAccess(DND_SYNC_WITH_THEATER_KEY)
                } else viewModel.setSyncWithTheater(it)
            }
        )
    }
}

/**
 * A Composable for displaying DnD Sync to Phone setting.
 * @param modifier [Modifier].
 * @param checked Whether the setting is checked.
 * @param enabled Whether the setting is enabled.
 * @param onCheckChanged Called when the check state changes.
 */
@Composable
fun SyncToPhoneSetting(
    modifier: Modifier = Modifier,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckChanged: (Boolean) -> Unit
) {
    CheckboxSetting(
        modifier = modifier,
        text = { Text(stringResource(R.string.pref_dnd_sync_to_phone_title)) },
        summary = {
            val text = if (enabled)
                stringResource(R.string.pref_dnd_sync_to_phone_summary)
            else
                stringResource(R.string.capability_not_supported)
            Text(text)
        },
        checked = checked,
        enabled = enabled,
        onCheckedChange = onCheckChanged
    )
}

/**
 * A Composable for displaying DnD Sync with Theater setting.
 * @param modifier [Modifier].
 * @param checked Whether the setting is checked.
 * @param enabled Whether the setting is enabled.
 * @param onCheckChanged Called when the check state changes.
 */
@Composable
fun SyncWithTheaterSetting(
    modifier: Modifier = Modifier,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckChanged: (Boolean) -> Unit
) {
    CheckboxSetting(
        modifier = modifier,
        text = { Text(stringResource(R.string.pref_dnd_sync_with_theater_title)) },
        summary = {
            val text = if (enabled)
                stringResource(R.string.pref_dnd_sync_with_theater_summary)
            else stringResource(R.string.capability_not_supported)
            Text(text)
        },
        checked = checked,
        enabled = enabled,
        onCheckedChange = onCheckChanged
    )
}
