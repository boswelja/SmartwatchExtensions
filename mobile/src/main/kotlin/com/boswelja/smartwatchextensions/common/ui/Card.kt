package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A card header with slots for a title, subtitle, icon, action.
 * @param modifier [Modifier].
 * @param title The header title.
 * @param subtitle The header subtitle.
 * @param icon The header icon.
 * @param action The header action.
 */
@Composable
fun CardHeader(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    subtitle: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(16.dp))
        }
        Column {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                title()
            }
            if (subtitle != null) {
                ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                    subtitle()
                }
            }
        }
        if (action != null) {
            Spacer(
                Modifier
                    .weight(1f)
                    .widthIn(min = 16.dp)
            )
            action()
        }
    }
}

/**
 * A card with a slot for a header.
 * @param modifier [Modifier].
 * @param header The card header Composable.
 * @param dividerVisible Whether the header divider is visible.
 * @param content The card content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Card(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    dividerVisible: Boolean = true,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = modifier
    ) {
        Column {
            header()
            if (dividerVisible) HorizontalDivider()
            content()
        }
    }
}
