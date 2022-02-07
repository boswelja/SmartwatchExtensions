package com.boswelja.smartwatchextensions.core.devicemanagement

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

/**
 * Contains state information for the paired phone.
 * @param id The paired phone UID.
 * @param name The paired phone name.
 * @param chargeNotiSent Whether a charge notification was sent for the phone.
 * @param lowNotiSent Whether a low notification was sent for the phone.
 */
@Serializable
data class PhoneState(
    val id: String,
    val name: String,
    val chargeNotiSent: Boolean,
    val lowNotiSent: Boolean
)

/**
 * A [DataStore]for tracking [PhoneState].
 */
val Context.phoneStateStore: DataStore<PhoneState> by dataStore(
    "phoneState.pb",
    PhoneStateSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { PhoneStateSerializer.defaultValue }
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
private object PhoneStateSerializer : Serializer<PhoneState> {
    override val defaultValue = PhoneState(
        id = "",
        name = "Phone",
        chargeNotiSent = false,
        lowNotiSent = false
    )

    override suspend fun readFrom(input: InputStream): PhoneState {
        try {
            return ProtoBuf.decodeFromByteArray(input.readBytes())
        } catch (_: Exception) {
            throw CorruptionException("PhoneState corrupted")
        }
    }

    override suspend fun writeTo(t: PhoneState, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(t))
    }
}
