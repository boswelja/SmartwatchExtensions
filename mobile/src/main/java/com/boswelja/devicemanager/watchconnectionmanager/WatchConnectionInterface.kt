package com.boswelja.devicemanager.watchconnectionmanager

interface WatchConnectionInterface {

    fun onWatchAdded()
    fun onWatchInfoUpdated()
    fun onConnectedWatchChanging()
    fun onConnectedWatchChanged(success: Boolean)

}