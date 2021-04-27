package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview

/**
 * [Crossflow] allows switching between two layouts with a slide in/out transition.
 * @param targetState is a key representing your target layout state. Every time you change a key
 * the animation will be triggered. The [content] called with the old key will be faded out while
 * the [content] called with the new key will be faded in.
 * @param modifier Modifier to be applied to the animation container.
 * @param animationSpec the [AnimationSpec] to configure the animation.
 */
@ExperimentalAnimationApi
@Composable
fun <T> Crossflow(
    targetState: T,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(easing = FastOutSlowInEasing),
    content: @Composable (T) -> Unit
) {
    val items = remember { mutableStateListOf<CrossflowAnimationItem<T>>() }
    val transitionState = remember { MutableTransitionState(targetState) }
    val targetChanged = (targetState != transitionState.targetState)
    val lastTarget = transitionState.targetState
    transitionState.targetState = targetState
    val transition = updateTransition(transitionState, label = "Transition")
    if (targetChanged || items.isEmpty()) {
        // Only manipulate the list when the state is changed, or in the first run.
        val keys = items.map { it.key }.run {
            if (!contains(targetState)) {
                toMutableList().also { it.add(targetState) }
            } else {
                this
            }
        }
        items.clear()
        keys.mapTo(items) { key ->
            CrossflowAnimationItem(key) {
                val width = LocalView.current.width
                val translationX by transition.animateFloat(
                    transitionSpec = { animationSpec },
                    label = "TranslationX"
                ) {
                    when {
                        it == lastTarget && targetChanged -> 1f
                        it != key -> -1f
                        else -> 0f
                    }
                }
                Box(
                    Modifier.graphicsLayer {
                        this.translationX = (translationX * width)
                    }
                ) {
                    content(key)
                }
            }
        }
    } else if (transitionState.currentState == transitionState.targetState) {
        // Remove all the intermediate items from the list once the animation is finished.
        items.removeAll { it.key != transitionState.targetState }
    }

    Box(modifier) {
        items.forEach {
            key(it.key) {
                it.content()
            }
        }
    }
}

@ExperimentalAnimationApi
@Preview(showBackground = true)
@Composable
fun CrossflowPreview() {
    var state by remember {
        mutableStateOf(false)
    }
    Crossflow(
        targetState = state,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        modifier = Modifier.clickable { state = !state }
    ) {
        if (it) {
            Box(Modifier.fillMaxSize().background(Color.Blue))
        } else {
            Box(Modifier.fillMaxSize().background(Color.Red))
        }
    }
}

private data class CrossflowAnimationItem<T>(
    val key: T,
    val content: @Composable () -> Unit
)
