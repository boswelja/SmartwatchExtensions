package com.boswelja.smartwatchextensions.extensions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.widget.ConfirmationOverlay
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.ui.BatteryStatsChip
import com.boswelja.smartwatchextensions.common.ui.showConfirmationOverlay
import com.boswelja.smartwatchextensions.phonelocking.ui.PhoneLockingChip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A Composable for displaying available Extensions.
 * @param modifier [Modifier].
 * @param extensionModifier A [Modifier] to be applied to each Extension Composable.
 * @param contentPadding The padding around the content.
 */
@Composable
fun Extensions(
    modifier: Modifier = Modifier,
    extensionModifier: Modifier = Modifier,
    contentPadding: Dp = 8.dp
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        val view = LocalView.current
        val coroutineScope = rememberCoroutineScope()
        val viewModel: ExtensionsViewModel = viewModel()

        val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState(false, Dispatchers.IO)
        val phoneLockingEnabled by viewModel.phoneLockingEnabled
            .collectAsState(false, Dispatchers.IO)
        val batteryPercent by viewModel.batteryPercent.collectAsState(0, Dispatchers.IO)
        val phoneName by viewModel.phoneName
            .collectAsState(stringResource(R.string.default_phone_name), Dispatchers.IO)

        BatteryStatsChip(
            modifier = extensionModifier,
            enabled = batterySyncEnabled,
            percent = batteryPercent,
            phoneName = phoneName,
            onClick=  {
                coroutineScope.launch {
                    val result = viewModel.requestBatteryStats()
                    if (result) {
                        view.showConfirmationOverlay(
                            type = ConfirmationOverlay.SUCCESS_ANIMATION,
                            message = view.context.getString(R.string.battery_sync_refresh_success)
                        )
                    } else {
                        view.showConfirmationOverlay(
                            type = ConfirmationOverlay.FAILURE_ANIMATION,
                            message = view.context.getString(R.string.phone_not_connected)
                        )
                    }
                }
            }
        )
        PhoneLockingChip(
            modifier = extensionModifier,
            enabled = phoneLockingEnabled,
            phoneName = phoneName
        ) {
            coroutineScope.launch {
                val result = viewModel.requestLockPhone()
                if (result) {
                    view.showConfirmationOverlay(
                        type = ConfirmationOverlay.SUCCESS_ANIMATION,
                        message = view.context.getString(R.string.lock_phone_success)
                    )
                } else {
                    view.showConfirmationOverlay(
                        type = ConfirmationOverlay.FAILURE_ANIMATION,
                        message = view.context.getString(R.string.phone_not_connected)
                    )
                }
            }
        }
    }
}
