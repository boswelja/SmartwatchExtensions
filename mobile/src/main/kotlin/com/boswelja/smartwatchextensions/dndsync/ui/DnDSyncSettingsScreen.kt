package com.boswelja.smartwatchextensions.dndsync.ui

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.smartwatchextensions.common.ui.SwitchPreference
import com.boswelja.smartwatchextensions.dndsync.ui.helper.DnDSyncHelperActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun DnDSyncSettingsScreen() {
    Column {
        val viewModel: DnDSyncSettingsViewModel = viewModel()
        val context = LocalContext.current

        var changingKey = remember<String?> { null }
        val notiPolicyAccessLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                changingKey?.let { key ->
                    if (viewModel.hasNotificationPolicyAccess) {
                        when (key) {
                            DND_SYNC_TO_PHONE_KEY -> viewModel.setSyncToPhone(true)
                            DND_SYNC_WITH_THEATER_KEY -> viewModel.setSyncWithTheater(true)
                        }
                    }
                    changingKey = null
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

        SyncToWatchSetting(viewModel = viewModel)
        SyncToPhoneSetting(
            viewModel = viewModel,
            onRequestNotificationPolicyAccess = onRequestNotiPolicyAccess
        )
        SyncWithTheaterSetting(
            viewModel = viewModel,
            onRequestNotificationPolicyAccess = onRequestNotiPolicyAccess
        )
    }
}

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun SyncToWatchSetting(
    viewModel: DnDSyncSettingsViewModel
) {
    val context = LocalContext.current
    val dndSyncToWatch by viewModel.syncToWatch.collectAsState(false)
    SwitchPreference(
        text = stringResource(R.string.pref_dnd_sync_to_watch_title),
        secondaryText = stringResource(R.string.pref_dnd_sync_to_watch_summary),
        isChecked = dndSyncToWatch,
        onCheckChanged = {
            if (!it) {
                viewModel.setSyncToWatch(false)
            } else {
                Intent(context, DnDSyncHelperActivity::class.java)
                    .also { intent -> context.startActivity(intent) }
            }
        }
    )
}

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun SyncToPhoneSetting(
    viewModel: DnDSyncSettingsViewModel,
    onRequestNotificationPolicyAccess: (String) -> Unit
) {
    val dndSyncToPhone by viewModel.syncToPhone.collectAsState(false)
    SwitchPreference(
        text = stringResource(R.string.pref_dnd_sync_to_phone_title),
        secondaryText = stringResource(R.string.pref_dnd_sync_to_phone_summary),
        isChecked = dndSyncToPhone,
        onCheckChanged = {
            if ((it && viewModel.hasNotificationPolicyAccess) || !it) {
                viewModel.setSyncToPhone(it)
            } else {
                onRequestNotificationPolicyAccess(DND_SYNC_TO_PHONE_KEY)
            }
        }
    )
}

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun SyncWithTheaterSetting(
    viewModel: DnDSyncSettingsViewModel,
    onRequestNotificationPolicyAccess: (String) -> Unit
) {
    val dndSyncWithTheater by viewModel.syncWithTheater.collectAsState(false)
    SwitchPreference(
        text = stringResource(R.string.pref_dnd_sync_with_theater_title),
        secondaryText = stringResource(R.string.pref_dnd_sync_with_theater_summary),
        isChecked = dndSyncWithTheater,
        onCheckChanged = {
            if ((it && viewModel.hasNotificationPolicyAccess) || !it) {
                viewModel.setSyncWithTheater(it)
            } else {
                onRequestNotificationPolicyAccess(DND_SYNC_WITH_THEATER_KEY)
            }
        }
    )
}
