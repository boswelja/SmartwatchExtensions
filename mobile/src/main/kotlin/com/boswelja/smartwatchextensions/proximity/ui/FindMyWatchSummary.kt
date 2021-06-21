package com.boswelja.smartwatchextensions.proximity.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.FeatureSummarySmall

@Preview(showBackground = true)
@Composable
fun FindMyWatchSummarySmall(modifier: Modifier = Modifier) {
    FeatureSummarySmall(
        modifier = modifier,
        icon = {
            Icon(
                painterResource(R.drawable.ic_find_watch),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .aspectRatio(1f)
            )
        },
        text = {
            Text(
                text = "Find my Watch",
                style = MaterialTheme.typography.h5
            )
        }
    )
}
