package com.boswelja.smartwatchextensions.core

/**
 * Models data for some feature, where said feature may be disabled and thus no data should be loaded.
 */
sealed class FeatureData<T>(
    open val data: T?,
    open val throwable: Throwable?
) {
    /**
     * Indicates the feature was disabled.
     */
    class Disabled<T>: FeatureData<T>(null, null)

    /**
     * Indicates the feature is enabled and data was loaded successfully.
     */
    class Success<T>(override val data: T?): FeatureData<T>(data, null)

    /**
     * Indicates there was an error loading the data.
     */
    class Error<T>(override val throwable: Throwable): FeatureData<T>(null, throwable)
}

fun <T> FeatureData<T>.getOrElse(onFailure: (Throwable?) -> T): T {
    return data ?: onFailure(throwable)
}
