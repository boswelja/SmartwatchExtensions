package com.boswelja.smartwatchextensions.aboutapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.boswelja.smartwatchextensions.common.ui.CardHeader

@Composable
fun AboutSectionCard(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Card(modifier) {
        Column {
            CardHeader(
                title = title
            )
            Divider()
            content()
        }
    }
}
