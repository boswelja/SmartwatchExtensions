package com.boswelja.devicemanager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.InputStream
import java.io.OutputStream

val Context.phoneStateStore: DataStore<PhoneState> by dataStore(
    "phoneState.pb",
    PhoneStateSerializer()
)

@Suppress("BlockingMethodInNonBlockingContext")
class PhoneStateSerializer : Serializer<PhoneState> {
    override val defaultValue = PhoneState(
        id = "",
        name = "Phone",
        batteryPercent = 0,
        chargeNotiSent = false,
        lowNotiSent = false
    )

    override suspend fun readFrom(input: InputStream): PhoneState =
        PhoneState.ADAPTER.decode(input)

    override suspend fun writeTo(t: PhoneState, output: OutputStream) {
        PhoneState.ADAPTER.encode(output, t)
    }
}
