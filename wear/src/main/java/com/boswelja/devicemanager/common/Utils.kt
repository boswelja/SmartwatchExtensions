package com.boswelja.devicemanager.common

import android.content.Context
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

object Utils {

    fun isCompanionAppInstalled(context: Context, capabilityCallbacks: CapabilityCallbacks) {
        Wearable
                .getCapabilityClient(context)
                .getCapability(
                    Config.CAPABILITY_PHONE_APP,
                    CapabilityClient.FILTER_REACHABLE
                )
                .addOnSuccessListener {
                    val node = it.nodes.lastOrNull()
                    if (node != null) {
                        capabilityCallbacks.capableDeviceFound(node)
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