package com.boswelja.devicemanager

import com.boswelja.devicemanager.common.prefsynclayer.BasePreferenceChangeReceiver

class RemotePreferenceChangeListener : BasePreferenceChangeReceiver() {

    override fun handleStartServices(dndSyncWithTheater: Boolean, batterySyncEnabled: Boolean) {
        if (batterySyncEnabled) {
            Utils.createBatterySyncJob(this)
        } else {
            Utils.stopBatterySyncJob(this)
        }
    }

}