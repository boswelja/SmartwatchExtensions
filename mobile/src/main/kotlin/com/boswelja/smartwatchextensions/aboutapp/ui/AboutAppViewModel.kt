package com.boswelja.smartwatchextensions.aboutapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boswelja.smartwatchextensions.devicemanagement.WatchManager
import com.boswelja.smartwatchextensions.versionsync.REQUEST_APP_VERSION
import com.boswelja.smartwatchextensions.versionsync.Version
import com.boswelja.smartwatchextensions.versionsync.VersionSerializer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class AboutAppViewModel internal constructor(
    application: Application
) : AndroidViewModel(application), DIAware {

    override val di: DI by closestDI()

    private val watchManager: WatchManager by instance()

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
            watchManager.incomingMessages(VersionSerializer)
                .collect { message -> _watchAppVersion.tryEmit(message.data) }
        }
    }
}
