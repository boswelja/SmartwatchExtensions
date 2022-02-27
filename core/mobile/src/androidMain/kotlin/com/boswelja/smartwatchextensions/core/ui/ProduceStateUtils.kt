package com.boswelja.smartwatchextensions.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

/**
 * Return a State that produces the elapsed minutes since this timestamp.
 */
@Composable
fun Long.toMinutes() = produceState(
    initialValue = TimeUnit.MINUTES.convert(
        System.currentTimeMillis() - this,
        TimeUnit.MILLISECONDS
    ).toInt(),
    key1 = this
) {
    while (true) {
        value = TimeUnit.MINUTES.convert(
            System.currentTimeMillis() - this@toMinutes,
            TimeUnit.MILLISECONDS
        ).toInt()
        delay(1.minutes)
    }
}
