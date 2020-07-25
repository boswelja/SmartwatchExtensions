/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager

import com.boswelja.devicemanager.watchmanager.item.Watch

@Deprecated("Use LiveData alternatives")
interface WatchConnectionListener {

    fun onWatchAdded(watch: Watch)
    fun onConnectedWatchChanging()
    fun onConnectedWatchChanged(isSuccess: Boolean)
}
