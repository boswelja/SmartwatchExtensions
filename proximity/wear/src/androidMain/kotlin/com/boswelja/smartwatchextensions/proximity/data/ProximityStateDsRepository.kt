package com.boswelja.smartwatchextensions.proximity.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.boswelja.smartwatchextensions.proximity.domain.ProximityStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream
import kotlin.Exception

/**
 * An implementation of [ProximityStateRepository] backed by a DataStore.
 */
class ProximityStateDsRepository(
    context: Context
) : ProximityStateRepository {

    private val store = context.proximityStateStore

    override fun getPhoneSeparationAlertEnabled(): Flow<Boolean> = store.data
        .map { it.phoneSeparationAlertsEnabled }

    override suspend fun setPhoneSeparationAlertEnabled(newValue: Boolean) {
        store.updateData { it.copy(phoneSeparationAlertsEnabled = newValue) }
    }
}

private data class ProximityState(
    val phoneSeparationAlertsEnabled: Boolean
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
private object ProximityStateSerializer : Serializer<ProximityState> {
    override val defaultValue: ProximityState =
        ProximityState(false)

    override suspend fun readFrom(input: InputStream): ProximityState {
        return try {
            ProtoBuf.decodeFromByteArray(input.readBytes())
        } catch (_: Exception) {
            throw CorruptionException("ProximityState store corrupted!")
        }
    }

    override suspend fun writeTo(t: ProximityState, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(t))
    }
}
private val Context.proximityStateStore by dataStore(
    "proximitystate.pb",
    ProximityStateSerializer
)