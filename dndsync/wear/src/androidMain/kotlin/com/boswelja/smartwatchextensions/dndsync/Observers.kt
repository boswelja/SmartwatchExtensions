package com.boswelja.smartwatchextensions.dndsync

import android.content.Context
import android.database.ContentObserver
import android.provider.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val THEATER_MODE = "theater_mode_on"

/**
 * Gets a [Flow] of this watches Theater Mode state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun Context.theaterMode(): Flow<Boolean> = callbackFlow {
    val uri = Settings.Global.getUriFor(THEATER_MODE)
    val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            if (!selfChange) {
                val isTheaterModeOn = isTheaterModeOn
                trySend(isTheaterModeOn)
            }
        }
    }
    contentResolver.registerContentObserver(uri, false, contentObserver)

    send(isTheaterModeOn)

    awaitClose {
        contentResolver.unregisterContentObserver(contentObserver)
    }
}
/**
 * Checks whether theater mode is currently enabled for this watch.
 */
val Context.isTheaterModeOn: Boolean
    get() = Settings.Global.getInt(contentResolver, THEATER_MODE, 0) == 1
