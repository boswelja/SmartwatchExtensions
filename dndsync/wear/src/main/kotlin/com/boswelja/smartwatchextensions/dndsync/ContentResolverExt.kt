package com.boswelja.smartwatchextensions.dndsync

import android.content.ContentResolver
import android.database.ContentObserver
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TheaterMode = "theater_mode_on"

/**
 * Gets a [Flow] of this watches Theater Mode state.
 */
fun ContentResolver.theaterMode(): Flow<Boolean> = callbackFlow {
    val uri = Settings.Global.getUriFor(TheaterMode)
    val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            if (!selfChange) {
                val isTheaterModeOn = isTheaterModeOn
                trySend(isTheaterModeOn)
            }
        }
    }
    registerContentObserver(uri, false, contentObserver)

    send(isTheaterModeOn)

    awaitClose {
        unregisterContentObserver(contentObserver)
    }
}
/**
 * Checks whether theater mode is currently enabled for this watch.
 */
val ContentResolver.isTheaterModeOn: Boolean
    get() = Settings.Global.getInt(this, TheaterMode, 0) == 1
