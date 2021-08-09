package com.boswelja.smartwatchextensions.dndsync.ui

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import com.boswelja.smartwatchextensions.common.ui.SwitchSetting
import com.boswelja.smartwatchextensions.dndsync.ui.helper.DnDSyncHelperActivity
import kotlinx.coroutines.Dispatchers

@Composable
fun DnDSyncSettingsScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        DndSyncSettingsCard()
    }
}

@Composable
fun DndSyncSettingsCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: DnDSyncSettingsViewModel = viewModel()
    var changingKey = rememberSaveable { "" }

    val canReceiveDnD by viewModel.canReceiveDnD.collectAsState(false, Dispatchers.IO)
    val canSendDnD by viewModel.canSendDnD.collectAsState(false, Dispatchers.IO)

    val syncToWatch by viewModel.syncToWatch.collectAsState(false, Dispatchers.IO)
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
        Toast.makeText(
            context,
            context.getString(R.string.dnd_sync_request_policy_access_message),
            Toast.LENGTH_SHORT
        ).show()
        notiPolicyAccessLauncher
            .launch(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }

    Card(
        modifier = modifier,
        header = {
            CardHeader(
                title = {
                    Text(stringResource(R.string.main_dnd_sync_title))
                }
            )
        }
    ) {
        Column {
            SyncToWatchSetting(
                enabled = canReceiveDnD,
                checked = syncToWatch,
                onCheckChanged = {
                    if (!it) {
                        viewModel.setSyncToWatch(false)
                    } else {
                        context.startActivity<DnDSyncHelperActivity>()
                    }
                }
            )
            SyncToPhoneSetting(
                enabled = canSendDnD,
                checked = syncToPhone,
                onCheckChanged = {
                    if (it && !viewModel.hasNotificationPolicyAccess) {
                        onRequestNotiPolicyAccess(DND_SYNC_TO_PHONE_KEY)
                    } else {
                        viewModel.setSyncToPhone(it)
                    }
                }
            )
            SyncWithTheaterSetting(
                enabled = canSendDnD,
                checked = syncWithTheater,
                onCheckChanged = {
                    if (it && !viewModel.hasNotificationPolicyAccess) {
                        onRequestNotiPolicyAccess(DND_SYNC_WITH_THEATER_KEY)
                    } else {
                        viewModel.setSyncWithTheater(it)
                    }
                }
            )
        }
    }
}

@Composable
fun SyncToWatchSetting(
    modifier: Modifier = Modifier,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckChanged: (Boolean) -> Unit
) {
    SwitchSetting(
        modifier = modifier,
        label = { Text(stringResource(R.string.pref_dnd_sync_to_watch_title)) },
        summary = {
            val text = if (enabled)
                stringResource(R.string.pref_dnd_sync_to_watch_summary)
            else
                stringResource(R.string.capability_not_supported)
            Text(text)
        },
        checked = checked,
        enabled = enabled,
        onCheckChanged = onCheckChanged
    )
}

@Composable
fun SyncToPhoneSetting(
    modifier: Modifier = Modifier,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckChanged: (Boolean) -> Unit
) {
    SwitchSetting(
        modifier = modifier,
        label = { Text(stringResource(R.string.pref_dnd_sync_to_phone_title)) },
        summary = {
            val text = if (enabled)
                stringResource(R.string.pref_dnd_sync_to_phone_summary)
            else
                stringResource(R.string.capability_not_supported)
            Text(text)
        },
        checked = checked,
        enabled = enabled,
        onCheckChanged = onCheckChanged
    )
}

@Composable
fun SyncWithTheaterSetting(
    modifier: Modifier = Modifier,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckChanged: (Boolean) -> Unit
) {
    SwitchSetting(
        modifier = modifier,
        label = { Text(stringResource(R.string.pref_dnd_sync_with_theater_title)) },
        summary = {
            val text = if (enabled)
                stringResource(R.string.pref_dnd_sync_with_theater_summary)
            else stringResource(R.string.capability_not_supported)
            Text(text)
        },
        checked = checked,
        enabled = enabled,
        onCheckChanged = onCheckChanged
    )
}
