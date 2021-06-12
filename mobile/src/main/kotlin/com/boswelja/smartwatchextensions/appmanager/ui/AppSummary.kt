package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AppSummarySmall(
    modifier: Modifier = Modifier,
    appCount: Int
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.Apps,
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Text(
            appCount.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5
        )
    }
}
