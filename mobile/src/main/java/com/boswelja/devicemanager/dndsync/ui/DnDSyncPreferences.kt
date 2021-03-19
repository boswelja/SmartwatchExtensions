package com.boswelja.devicemanager.dndsync.ui

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.registerForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.common.rememberSharedPreferences
import com.boswelja.devicemanager.common.ui.SwitchPreference
import com.boswelja.devicemanager.dndsync.ui.helper.DnDSyncHelperActivity
import com.boswelja.devicemanager.watchmanager.WatchManager
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun DnDSyncPreferences() {
    Column {
        val sharedPreferences = rememberSharedPreferences()
        val coroutineScope = rememberCoroutineScope()
        val watchManager = WatchManager.getInstance(LocalContext.current)
        val context = LocalContext.current

        var dndSyncToWatch by remember {
            mutableStateOf(sharedPreferences.getBoolean(DND_SYNC_TO_WATCH_KEY, false))
        }
        var dndSyncToPhone by remember {
            mutableStateOf(sharedPreferences.getBoolean(DND_SYNC_TO_PHONE_KEY, false))
        }
        var dndSyncWithTheater by remember {
            mutableStateOf(sharedPreferences.getBoolean(DND_SYNC_WITH_THEATER_KEY, false))
        }

        var changingKey = remember<String?> { null }
        val notiPolicyAccessLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                changingKey?.let {
                    if (Compat.canSetDnD(context)) {
                        when (it) {
                            DND_SYNC_TO_PHONE_KEY -> dndSyncToPhone = true
                            DND_SYNC_WITH_THEATER_KEY -> dndSyncWithTheater = true
                        }
                        coroutineScope.launch {
                            sharedPreferences.edit(commit = true) { putBoolean(it, true) }
                            watchManager.updatePreference(
                                watchManager.selectedWatch.value!!,
                                it,
                                true
                            )
                        }
                    }
                    changingKey = null
                }
            }

        SwitchPreference(
            text = stringResource(R.string.pref_dnd_sync_to_watch_title),
            secondaryText = stringResource(R.string.pref_dnd_sync_to_watch_summary),
            isChecked = dndSyncToWatch,
            onCheckChanged = {
                if (!it) {
                    coroutineScope.launch {
                        sharedPreferences.edit(commit = true) {
                            putBoolean(DND_SYNC_TO_WATCH_KEY, it)
                        }
                        watchManager.updatePreference(
                            watchManager.selectedWatch.value!!,
                            DND_SYNC_TO_WATCH_KEY,
                            it
                        )
                    }
                    dndSyncToWatch = it
                } else {
                    Intent(context, DnDSyncHelperActivity::class.java)
                        .also { intent -> context.startActivity(intent) }
                }
            }
        )
        SwitchPreference(
            text = stringResource(R.string.pref_dnd_sync_to_phone_title),
            secondaryText = stringResource(R.string.pref_dnd_sync_to_phone_summary),
            isChecked = dndSyncToPhone,
            onCheckChanged = {
                if ((it && Compat.canSetDnD(context)) || !it) {
                    coroutineScope.launch {
                        sharedPreferences.edit(commit = true) {
                            putBoolean(DND_SYNC_TO_PHONE_KEY, it)
                        }
                        watchManager.updatePreference(
                            watchManager.selectedWatch.value!!,
                            DND_SYNC_TO_PHONE_KEY,
                            it
                        )
                    }
                    dndSyncToPhone = it
                } else {
                    changingKey = DND_SYNC_TO_PHONE_KEY
                    Toast.makeText(
                        context,
                        context.getString(R.string.dnd_sync_request_policy_access_message),
                        Toast.LENGTH_SHORT
                    ).show()
                    notiPolicyAccessLauncher
                        .launch(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                }
            }
        )
        SwitchPreference(
            text = stringResource(R.string.pref_dnd_sync_with_theater_title),
            secondaryText = stringResource(R.string.pref_dnd_sync_with_theater_summary),
            isChecked = dndSyncWithTheater,
            onCheckChanged = {
                if ((it && Compat.canSetDnD(context)) || !it) {
                    coroutineScope.launch {
                        sharedPreferences.edit(commit = true) {
                            putBoolean(DND_SYNC_WITH_THEATER_KEY, it)
                        }
                        watchManager.updatePreference(
                            watchManager.selectedWatch.value!!,
                            DND_SYNC_WITH_THEATER_KEY,
                            it
                        )
                    }
                    dndSyncWithTheater = it
                } else {
                    changingKey = DND_SYNC_WITH_THEATER_KEY
                    Toast.makeText(
                        context,
                        context.getString(R.string.dnd_sync_request_policy_access_message),
                        Toast.LENGTH_SHORT
                    ).show()
                    notiPolicyAccessLauncher
                        .launch(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                }
            }
        )
    }
}
