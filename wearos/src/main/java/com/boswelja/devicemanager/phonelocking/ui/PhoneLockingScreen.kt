package com.boswelja.devicemanager.phonelocking.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhonelinkLock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.devicemanager.R

@ExperimentalMaterialApi
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

@ExperimentalMaterialApi
@Composable
fun PhoneLockingDisabled() {
    ListItem(
        text = { Text(stringResource(R.string.lock_phone_disabled)) },
        icon = {
            Icon(
                Icons.Outlined.PhonelinkLock,
                null,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }
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
            Modifier.size(56.dp)
        )
        Text(
            stringResource(R.string.lock_phone, phoneName),
            style = MaterialTheme.typography.h6
        )
    }
}
