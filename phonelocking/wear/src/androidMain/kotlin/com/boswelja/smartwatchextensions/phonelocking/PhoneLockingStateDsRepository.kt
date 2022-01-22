package com.boswelja.smartwatchextensions.phonelocking

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

/**
 * An implementation of [PhoneLockingStateRepository] that makes use of a DataStore for saving/loading data.
 */
class PhoneLockingStateDsRepository(context: Context) : PhoneLockingStateRepository {

    private val stateStore = context.phoneLockingStateStore

    override fun getPhoneLockingState(): Flow<PhoneLockingState> {
        return stateStore.data
    }

    override suspend fun updatePhoneLockingState(block: (PhoneLockingState) -> PhoneLockingState) {
        stateStore.updateData(block)
    }
}

private val Context.phoneLockingStateStore by dataStore(
    "phonelockingstate.pb",
    PhoneLockingStateSerializer
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
private object PhoneLockingStateSerializer : Serializer<PhoneLockingState> {
    override val defaultValue: PhoneLockingState = PhoneLockingState(false)

    override suspend fun readFrom(input: InputStream): PhoneLockingState {
        return ProtoBuf.decodeFromByteArray(input.readBytes())
    }

    override suspend fun writeTo(t: PhoneLockingState, output: OutputStream) {
        output.write(ProtoBuf.encodeToByteArray(t))
    }
}
