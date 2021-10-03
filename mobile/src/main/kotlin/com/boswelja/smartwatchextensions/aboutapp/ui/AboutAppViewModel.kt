package com.boswelja.smartwatchextensions.aboutapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.common.connection.Messages.REQUEST_APP_VERSION
import com.boswelja.smartwatchextensions.common.versioning.Version
import com.boswelja.smartwatchextensions.common.versioning.VersionSerializer
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class AboutAppViewModel internal constructor(
    application: Application,
    private val watchManager: WatchManager
) : AndroidViewModel(application) {

    @Suppress("unused")
    constructor(application: Application) : this(
        application,
        WatchManager.getInstance(application)
    )

    private val _watchAppVersion = MutableStateFlow<Version?>(null)
    val watchAppVersion: Flow<Version?>
        get() = _watchAppVersion

    init {
        // Send app version request to selected watches
        viewModelScope.launch {
            watchManager.selectedWatch.collect { watch ->
                if (watch?.uid != null) {
                    watchManager.sendMessage(watch, REQUEST_APP_VERSION, null)
                    _watchAppVersion.emit(null)
                } else {
                    Timber.w("Selected watch null")
                    _watchAppVersion.emit(null)
                }
            }
        }

        // Listen for app version responses
        viewModelScope.launch {
            watchManager.incomingMessages<Version>(VersionSerializer)
                .collect { message -> _watchAppVersion.tryEmit(message.data) }
        }
    }
}
