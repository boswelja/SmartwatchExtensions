package com.boswelja.devicemanager

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.CapabilityApi
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

object Utils {

    fun isCompanionAppInstalled(googleApiClient: GoogleApiClient, capabilityCallbacks: CapabilityCallbacks) {
        Wearable.CapabilityApi.getCapability(
                googleApiClient,
                Config.CAPABILITY_PHONE_APP,
                CapabilityApi.FILTER_REACHABLE)
                .setResultCallback { getCapabilityResult ->
                    if (getCapabilityResult.status.isSuccess) {
                        val node = getCapabilityResult.capability.nodes.lastOrNull()
                        if (node != null) {
                            capabilityCallbacks.capableDeviceFound(node)
                        } else {
                            capabilityCallbacks.noCapableDevices()
                        }
                    } else {
                        capabilityCallbacks.noCapableDevices()
                    }
                }
    }

    interface CapabilityCallbacks {

        fun capableDeviceFound(node: Node?)

        fun noCapableDevices()

    }

}