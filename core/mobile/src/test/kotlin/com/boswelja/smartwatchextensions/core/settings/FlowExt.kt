package com.boswelja.smartwatchextensions.core.settings

import app.cash.turbine.FlowTurbine
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Workaround for Turbine breaking with runTest, see https://github.com/cashapp/turbine/issues/42#issuecomment-1000317026
 */
@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
suspend fun <T> Flow<T>.test2(
    timeout: Duration = 1.seconds,
    validate: suspend FlowTurbine<T>.() -> Unit,
) {
    withContext(Dispatchers.Default) {
        test(timeout, validate)
    }
}
