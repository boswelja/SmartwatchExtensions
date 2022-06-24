package com.boswelja.smartwatchextensions.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Maps emitted values of type [T] to a Result of type [R] via the given [transform]. Upstream exceptions are caught via
 * [catch] and emitted as [Result.failure].
 */
fun <T, R> Flow<T>.mapCatching(transform: (T) -> R): Flow<Result<R>> = this
    .map { data -> Result.success(transform(data)) }
    .catch { cause -> emit(Result.failure(cause)) }

/**
 * Maps emitted values of type [T] to a Result. Upstream exceptions are caught via [catch] and emitted as
 * [Result.failure].
 */
fun <T> Flow<T>.runCatching(): Flow<Result<T>> = this
    .mapCatching { it }
