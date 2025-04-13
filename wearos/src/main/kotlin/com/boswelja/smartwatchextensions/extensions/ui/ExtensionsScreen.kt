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
import androidx.wear.widget.ConfirmationOverlay
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncChip
import com.boswelja.smartwatchextensions.common.ui.showConfirmationOverlay
import com.boswelja.smartwatchextensions.phonelocking.ui.PhoneLockingChip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

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
        val viewModel: ExtensionsViewModel = koinViewModel()

        val phoneLockingEnabled by viewModel.phoneLockingEnabled.collectAsState()
        val phoneName by viewModel.phoneName
            .collectAsState(stringResource(R.string.default_phone_name), Dispatchers.IO)

        BatterySyncChip(
            modifier = extensionModifier,
            phoneName = phoneName
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
                        message = view.context.getString(
                            com.boswelja.smartwatchextensions.phonelocking.R.string.lock_phone_success
                        )
                    )
                } else {
                    view.showConfirmationOverlay(
                        type = ConfirmationOverlay.FAILURE_ANIMATION,
                        message = view.context.getString(
                            com.boswelja.smartwatchextensions.batterysync.R.string.phone_not_connected
                        )
                    )
                }
            }
        }
    }
}
