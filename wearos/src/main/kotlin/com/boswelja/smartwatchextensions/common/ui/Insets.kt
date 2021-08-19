package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

object RoundScreenDefaults {
    val VerticalPadding = 64.dp
    val HorizontalPadding = 8.dp
}

object SquareScreenDefaults {
    val VerticalPadding = 8.dp
    val HorizontalPadding = 8.dp
}

/**
 * Applies different [PaddingValues] based on [isRoundScreen].
 * @param roundPaddingValues The [PaddingValues] to be applied on devices with a round screen.
 * @param squarePaddingValues The [PaddingValues] to be applied on devices with a square screen.
 */
@Stable
fun Modifier.roundScreenPadding(
    roundPaddingValues: PaddingValues = PaddingValues(
        vertical = RoundScreenDefaults.VerticalPadding,
        horizontal = RoundScreenDefaults.HorizontalPadding
    ),
    squarePaddingValues: PaddingValues = PaddingValues(
        vertical = SquareScreenDefaults.VerticalPadding,
        horizontal = SquareScreenDefaults.HorizontalPadding
    )
): Modifier = composed {
    if (isRoundScreen()) {
        padding(roundPaddingValues)
    } else {
        padding(squarePaddingValues)
    }
}

/**
 * Returns the value of [android.content.res.Configuration.isScreenRound].
 */
@Composable
fun isRoundScreen(): Boolean {
    return LocalContext.current.resources.configuration.isScreenRound
}
