package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A vertical grid that contains children of varied height and staggers them in the layout.
 * @param modifier [Modifier].
 * @param cells The [GridCells] to calculate child width by.
 * @param contentSpacing The spacing between child Composables.
 * @param content The grid content.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StaggeredVerticalGrid(
    modifier: Modifier = Modifier,
    cells: GridCells,
    contentSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        check(constraints.hasBoundedWidth) {
            "Unbounded width not supported"
        }

        // Calculate column count, width and spacing
        val contentSpacingPx = contentSpacing.roundToPx()
        val (columns, columnWidth) = when (cells) {
            is GridCells.Adaptive -> {
                val columns = constraints.maxWidth / cells.minSize.roundToPx()
                val columnWidth =
                    (constraints.maxWidth - contentSpacingPx * (columns - 1)) / columns
                Pair(columns, columnWidth)
            }
            is GridCells.Fixed -> {
                Pair(cells.count, constraints.maxWidth / cells.count)
            }
        }

        // Calculate placeable location
        val itemConstraints = constraints.copy(maxWidth = columnWidth)
        val colHeights = IntArray(columns) { 0 } // track each column's height
        val placeableXY: MutableMap<Placeable, Pair<Int, Int>> = mutableMapOf()
        val placeables = measurables.map { measurable ->
            val column = shortestColumn(colHeights)
            val placeable = measurable.measure(itemConstraints)
            val x = columnWidth * column + contentSpacingPx * column
            placeableXY[placeable] = Pair(x, colHeights[column])
            colHeights[column] += placeable.height + contentSpacingPx
            placeable
        }

        // Get the height of the content
        val height = colHeights.maxOrNull()?.coerceIn(constraints.minHeight, constraints.maxHeight)
            ?: constraints.minHeight

        // Draw the layout
        layout(
            width = constraints.maxWidth,
            height = height
        ) {
            placeables.forEach { placeable ->
                val xy = placeableXY[placeable]!!
                placeable.place(
                    x = xy.first,
                    y = xy.second
                )
            }
        }
    }
}

private fun shortestColumn(colHeights: IntArray): Int {
    var minHeight = Int.MAX_VALUE
    var column = 0
    colHeights.forEachIndexed { index, height ->
        if (height < minHeight) {
            minHeight = height
            column = index
        }
    }
    return column
}
