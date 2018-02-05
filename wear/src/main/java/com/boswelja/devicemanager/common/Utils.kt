package com.boswelja.devicemanager.common

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.CapabilityApi
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

object Utils {

    fun isCompanionAppInstalled(context: Context, capabilityCallbacks: CapabilityCallbacks) {
        //Wearable.CapabilityApi.getCapability(
        //        googleApiClient,
        //        Config.CAPABILITY_PHONE_APP,
        //        CapabilityApi.FILTER_REACHABLE)
        //        .setResultCallback { getCapabilityResult ->
        //            if (getCapabilityResult.status.isSuccess) {
        //                val node = getCapabilityResult.capability.nodes.lastOrNull()
        //                if (node != null) {
        //                    capabilityCallbacks.capableDeviceFound(node)
        //                } else {
        //                    capabilityCallbacks.noCapableDevices()
        //                }
        //            } else {
        //                capabilityCallbacks.noCapableDevices()
        //            }
        //        }
        val capabilityInfo = Task.await(Wearable
            .getCapabilityClient(context)
            .getCapability(
                Config.CAPABILITY_PHONE_APP,
                CapabilityClient.FILTER_REACHABLE
            ))
        val node = capabilityInfo.nodes.lastOrNull()
        if (node != null) {
            capabilityCallbacks.capableDeviceFound(node)
        } else {
            capabilityCallbacks.noCapableDevices()
        }
    }

    interface CapabilityCallbacks {

        fun capableDeviceFound(node: Node?)

        fun noCapableDevices()

    }

}