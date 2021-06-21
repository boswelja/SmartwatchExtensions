package com.boswelja.smartwatchextensions.common.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.launch

@ExperimentalTime
@Composable
fun animateCountdownAsState(
    countdownTime: Duration,
    onFinished: (() -> Unit)? = null
): State<Float> {
    val animatable = remember { Animatable(1f) }
    LaunchedEffect("animation") {
        launch {
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = countdownTime.inWholeMilliseconds.toInt(),
                    easing = LinearEasing
                )
            )
            onFinished?.invoke()
        }
    }
    return animatable.asState()
}

@ExperimentalTime
@Composable
fun CircularCountDownTimer(
    modifier: Modifier = Modifier,
    time: Duration,
    onFinished: (() -> Unit)? = null
) {
    val timeSeconds = time.inWholeSeconds
    val remainingProgress by animateCountdownAsState(
        countdownTime = time,
        onFinished = onFinished
    )
    val remainingTime = floor(timeSeconds * remainingProgress).toInt()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f),
            progress = remainingProgress,
            strokeWidth = 8.dp
        )
        Text(
            text = remainingTime.toString(),
            style = MaterialTheme.typography.h2
        )
    }
}
