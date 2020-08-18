/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchsetup.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WatchSetupViewModel(application: Application) : AndroidViewModel(application) {

  private val coroutineJob = Job()
  private val coroutineScope = CoroutineScope(Dispatchers.IO + coroutineJob)
  private val watchManager = WatchManager.get(application)

  private val _availableWatches = MutableLiveData<List<Watch>?>(null)
  val availableWatches: LiveData<List<Watch>?>
    get() = _availableWatches

  private val _isLoading = MutableLiveData(true)
  val isLoading: LiveData<Boolean>
    get() = _isLoading

  private val _finishActivity = MutableLiveData(false)
  val finishActivity: LiveData<Boolean>
    get() = _finishActivity

  init {
    refreshAvailableWatches()
  }

  fun refreshAvailableWatches() {
    _isLoading.postValue(true)
    coroutineScope.launch {
      val availableWatches = watchManager.getAvailableWatches()
      _availableWatches.postValue(availableWatches)
      _isLoading.postValue(false)
    }
  }

  fun registerWatch(watch: Watch) {
    coroutineScope.launch {
      watchManager.registerWatch(watch)
      _finishActivity.postValue(true)
    }
  }

  override fun onCleared() {
    super.onCleared()
    coroutineJob.cancel()
  }
}
