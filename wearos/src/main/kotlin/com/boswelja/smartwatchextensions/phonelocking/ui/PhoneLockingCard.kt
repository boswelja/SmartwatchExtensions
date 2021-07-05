package com.boswelja.smartwatchextensions.phonelocking.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhonelinkLock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ExtensionCard

@Composable
fun PhoneLockingCard(
    modifier: Modifier = Modifier,
    phoneName: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    ExtensionCard(
        modifier = modifier,
        icon = { size ->
            Icon(
                Icons.Outlined.PhonelinkLock,
                null,
                Modifier.size(size),
                tint = Color.White
            )
        },
        hintText = {
            Text(
                stringResource(R.string.lock_phone_disabled, phoneName),
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center
            )
        },
        content = {
            if (enabled) {
                Text(
                    stringResource(R.string.lock_phone, phoneName),
                    style = MaterialTheme.typography.display3,
                    textAlign = TextAlign.Center
                )
            }
        },
        onClick = onClick,
        enabled = enabled
    )
}
