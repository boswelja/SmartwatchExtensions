package com.boswelja.smartwatchextensions.dndsync

import android.content.Context
import android.database.ContentObserver
import android.provider.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

const val THEATER_MODE = "theater_mode_on"

/**
 * Gets a [Flow] of this watches Theater Mode state.
 */
@ExperimentalCoroutinesApi
fun Context.theaterMode(): Flow<Boolean> = callbackFlow {
    Timber.d("Starting theater_mode_on collector flow")
    val uri = Settings.Global.getUriFor(THEATER_MODE)
    val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            Timber.d("onChange(%s) called", selfChange)
            if (!selfChange) {
                val isTheaterModeOn = isTheaterModeOn
                Timber.d("isTheaterModeOn = %s", isTheaterModeOn)
                trySend(isTheaterModeOn)
            }
        }
    }
    contentResolver.registerContentObserver(uri, false, contentObserver)

    send(isTheaterModeOn)

    awaitClose {
        Timber.d("Stopping theater_mode_on collector flow")
        contentResolver.unregisterContentObserver(contentObserver)
    }
}
/**
 * Checks whether theater mode is currently enabled for this watch.
 */
val Context.isTheaterModeOn: Boolean
    get() = Settings.Global.getInt(contentResolver, THEATER_MODE, 0) == 1
