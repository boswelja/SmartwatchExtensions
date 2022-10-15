package com.boswelja.smartwatchextensions.wearableinterface.playservices

import android.content.Context
import com.boswelja.smartwatchextensions.wearableinterface.ConnectionMode
import com.boswelja.smartwatchextensions.wearableinterface.Phone
import com.boswelja.smartwatchextensions.wearableinterface.PhoneRepository
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

internal class PhoneRepositoryImpl(
    context: Context,
    private val phoneAppCapability: String
) : PhoneRepository {

    private val capabilityClient = Wearable.getCapabilityClient(context)

    override suspend fun getPairedPhone(): Phone {
        val connectedPhones = capabilityClient.getCapability(phoneAppCapability, CapabilityClient.FILTER_ALL).await()
        val connectedPhone = connectedPhones.nodes.first()
        return Phone(
            connectedPhone.id,
            connectedPhone.displayName,
            if (connectedPhone.isNearby) ConnectionMode.Bluetooth else ConnectionMode.Network
        )
    }
}
