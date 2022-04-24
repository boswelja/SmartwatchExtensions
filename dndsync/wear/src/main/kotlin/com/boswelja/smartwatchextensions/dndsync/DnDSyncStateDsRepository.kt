package com.boswelja.smartwatchextensions.dndsync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

private val Context.dndSyncStateStore: DataStore<DnDSyncState> by dataStore(
    "batterysyncstate.pb",
    BatterySyncStateSerializer
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
private object BatterySyncStateSerializer : Serializer<DnDSyncState> {
    override val defaultValue = DnDSyncState(
        dndSyncToPhone = false,
        dndSyncWithTheater = false
    )

    override suspend fun readFrom(input: InputStream): DnDSyncState =
        ProtoBuf.decodeFromByteArray(input.readBytes())

    override suspend fun writeTo(t: DnDSyncState, output: OutputStream) =
        output.write(ProtoBuf.encodeToByteArray(t))
}

/**
 * A [DnDSyncStateRepository] backed by a DataStore.
 */
class DnDSyncStateDsRepository(context: Context) : DnDSyncStateRepository {

    private val dataStore = context.dndSyncStateStore

    override fun getDnDSyncState(): Flow<DnDSyncState> = dataStore.data

    override suspend fun updateDnDSyncState(block: (DnDSyncState) -> DnDSyncState) {
        dataStore.updateData(block)
    }
}
