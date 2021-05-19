package com.boswelja.smartwatchextensions.dndsync

import android.content.ContentResolver
import android.database.ContentObserver
import android.provider.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

object TheaterModeObserver {
    const val THEATER_MODE_ON = "theater_mode_on"

    @ExperimentalCoroutinesApi
    fun theaterMode(contentResolver: ContentResolver): Flow<Boolean> = callbackFlow {
        Timber.d("Starting theater_mode_on collector flow")
        val uri = Settings.Global.getUriFor(THEATER_MODE_ON)
        val contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                Timber.d("onChange(%s) called", selfChange)
                if (!selfChange) {
                    val isTheaterModeOn = isTheaterModeOn(contentResolver)
                    Timber.d("isTheaterModeOn = %s", isTheaterModeOn)
                    sendBlocking(isTheaterModeOn)
                }
            }
        }
        contentResolver.registerContentObserver(uri, false, contentObserver)
        awaitClose {
            Timber.d("Stopping theater_mode_on collector flow")
            contentResolver.unregisterContentObserver(contentObserver)
        }
    }

    private fun isTheaterModeOn(contentResolver: ContentResolver): Boolean =
        Settings.Global.getInt(contentResolver, THEATER_MODE_ON, 0) == 1
}
