package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * Displays a summary of the Phone Locking feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLockingSummary(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(Modifier.fillMaxWidth().then(contentModifier)) {
            Text(
                text = stringResource(com.boswelja.smartwatchextensions.phonelocking.R.string.main_phone_locking_title),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
