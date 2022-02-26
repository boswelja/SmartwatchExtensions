package com.boswelja.smartwatchextensions.proximity.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.core.ui.settings.CheckboxSetting
import com.boswelja.smartwatchextensions.proximity.R
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

/**
 * A Composable screen for displaying proximity settings.
 * @param modifier [Modifier].
 */
@Composable
fun ProximitySettingsScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val viewModel = getViewModel<ProximitySettingsViewModel>()
    val phoneProximityNotiEnabled by viewModel.phoneProximityNotiSetting.collectAsState()
    val watchProximityNotiEnabled by viewModel.watchProximityNotiSetting.collectAsState()

    Column(modifier) {
        CheckboxSetting(
            text = { Text(stringResource(R.string.proximity_phone_noti_title)) },
            summary = { Text(stringResource(R.string.proximity_phone_noti_summary)) },
            checked = phoneProximityNotiEnabled,
            onCheckedChange = { isChecked ->
                scope.launch {
                    viewModel.setPhoneProximityNotiEnabled(isChecked)
                }
            }
        )
        CheckboxSetting(
            text = { Text(stringResource(R.string.proximity_watch_noti_title)) },
            summary = { Text(stringResource(R.string.proximity_watch_noti_summary)) },
            checked = watchProximityNotiEnabled,
            onCheckedChange = { isChecked ->
                scope.launch {
                    viewModel.setWatchProximityNotiEnabled(isChecked)
                }
            }
        )
    }
}
