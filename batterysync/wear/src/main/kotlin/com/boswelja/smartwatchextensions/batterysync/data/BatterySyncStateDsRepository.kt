package com.boswelja.smartwatchextensions.batterysync.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.boswelja.smartwatchextensions.batterysync.domain.BatterySyncState
import com.boswelja.smartwatchextensions.batterysync.domain.BatterySyncStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

private val Context.batterySyncStateStore: DataStore<BatterySyncState> by dataStore(
    "batterysyncstate.pb",
    BatterySyncStateSerializer
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
private object BatterySyncStateSerializer : Serializer<BatterySyncState> {
    override val defaultValue = BatterySyncState(
        batterySyncEnabled = false,
        phoneChargeNotificationEnabled = false,
        phoneLowNotificationEnabled = false,
        phoneChargeThreshold = 90,
        phoneLowThreshold = 15,
        notificationPosted = false
    )

    override suspend fun readFrom(input: InputStream): BatterySyncState =
        ProtoBuf.decodeFromByteArray(input.readBytes())

    override suspend fun writeTo(t: BatterySyncState, output: OutputStream) =
        output.write(ProtoBuf.encodeToByteArray(t))
}

/**
 * A [BatterySyncStateRepository] backed by a DataStore.
 */
class BatterySyncStateDsRepository(context: Context) : BatterySyncStateRepository {

    private val dataStore = context.batterySyncStateStore

    override fun getBatterySyncState(): Flow<BatterySyncState> = dataStore.data

    override suspend fun updateBatterySyncState(block: (BatterySyncState) -> BatterySyncState) {
        dataStore.updateData(block)
    }
}
