package com.boswelja.smartwatchextensions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.InputStream
import java.io.OutputStream

/**
 * A [DataStore]for tracking [PhoneState].
 */
val Context.phoneStateStore: DataStore<PhoneState> by dataStore(
    "phoneState.pb",
    PhoneStateSerializer()
)

@Suppress("BlockingMethodInNonBlockingContext")
private class PhoneStateSerializer : Serializer<PhoneState> {
    override val defaultValue = PhoneState(
        id = "",
        name = "Phone",
        chargeNotiSent = false,
        lowNotiSent = false
    )

    override suspend fun readFrom(input: InputStream): PhoneState =
        PhoneState.ADAPTER.decode(input)

    override suspend fun writeTo(t: PhoneState, output: OutputStream) {
        PhoneState.ADAPTER.encode(output, t)
    }
}
