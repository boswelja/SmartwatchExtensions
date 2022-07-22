package com.boswelja.smartwatchextensions.core

inline fun <T> FeatureData<T>?.fold(
    success: (T) -> Unit,
    error: (Throwable) -> Unit,
    disabled: () -> Unit,
    loading: () -> Unit
) {
    when (this) {
        is FeatureData.Success -> success(data)
        is FeatureData.Disabled -> disabled()
        is FeatureData.Error -> error(throwable)
        null -> loading()
    }
}
