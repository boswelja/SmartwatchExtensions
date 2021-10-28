package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
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
            ProvideTextStyle(MaterialTheme.typography.subtitle1) {
                title()
            }
            if (subtitle != null) {
                ProvideTextStyle(MaterialTheme.typography.subtitle2) {
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
 * @param shape The card shape.
 * @param backgroundColor The card background color.
 * @param contentColor The card content color.
 * @param border The card border stroke.
 * @param elevation The card elevation.
 * @param dividerVisible Whether the header divider is visible.
 * @param content The card content.
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    border: BorderStroke? = null,
    elevation: Dp = 1.dp,
    dividerVisible: Boolean = true,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        border = border,
        elevation = elevation
    ) {
        Column {
            header()
            if (dividerVisible) Divider()
            content()
        }
    }
}

/**
 * A card with support for expanding & collapsing.
 * @param modifier [Modifier].
 * @param title The card title.
 * @param shape The card shape.
 * @param backgroundColor The card background color.
 * @param contentColor The card content color.
 * @param border The card border stroke.
 * @param elevation The card elevation.
 * @param expanded Whether the card is currently expanded.
 * @param toggleExpanded Called when the card expanded state should be toggled.
 * @param content The card content.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    border: BorderStroke? = null,
    elevation: Dp = 1.dp,
    expanded: Boolean,
    toggleExpanded: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        border = border,
        elevation = elevation,
        dividerVisible = expanded,
        header = {
            CardHeader(
                modifier = Modifier.clickable(onClick = toggleExpanded),
                title = title,
                action = {
                    Crossfade(targetState = expanded) {
                        if (it) {
                            Icon(Icons.Outlined.ExpandLess, null)
                        } else {
                            Icon(Icons.Outlined.ExpandMore, null)
                        }
                    }
                }
            )
        },
        content = {
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                content()
            }
        }
    )
}
