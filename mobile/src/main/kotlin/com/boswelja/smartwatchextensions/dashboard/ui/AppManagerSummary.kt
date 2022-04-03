package com.boswelja.smartwatchextensions.dashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R

/**
 * A Composable for displaying a summary of apps installed on a watch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerSummary(
    userAppCount: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(Modifier.fillMaxWidth().then(contentModifier)) {
            Text(
                text = stringResource(R.string.main_app_manager_title),
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(Modifier.height(2.dp))

            Text(
                context.resources.getQuantityString(
                    R.plurals.app_manager_app_count, userAppCount.toInt(), userAppCount
                ),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
