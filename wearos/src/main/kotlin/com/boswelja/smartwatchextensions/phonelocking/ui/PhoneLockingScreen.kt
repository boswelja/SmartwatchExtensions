package com.boswelja.smartwatchextensions.phonelocking.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhonelinkLock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.boswelja.smartwatchextensions.R

@Composable
fun PhoneLockingScreen(
    phoneLockingEnabled: Boolean,
    phoneName: String,
    onClick: () -> Unit
) {
    if (phoneLockingEnabled) {
        PhoneLockingEnabled(
            phoneName = phoneName,
            onClick = onClick
        )
    } else {
        PhoneLockingDisabled()
    }
}

@Composable
fun PhoneLockingDisabled() {
    Text(
        modifier = Modifier.padding(16.dp),
        text = stringResource(R.string.lock_phone_disabled)
    )
}

@Composable
fun PhoneLockingEnabled(
    phoneName: String,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.PhonelinkLock,
            null,
            Modifier.size(56.dp),
            tint = Color.White
        )
        Text(
            stringResource(R.string.lock_phone, phoneName),
            style = MaterialTheme.typography.display2,
            textAlign = TextAlign.Center
        )
    }
}
