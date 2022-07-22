package com.boswelja.smartwatchextensions.batterysync.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.boswelja.smartwatchextensions.batterysync.domain.model.BatterySyncConfig
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatterySyncConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

private val Context.batterySyncConfigStore: DataStore<BatterySyncConfig> by dataStore(
    "batterysyncstate.pb",
    BatterySyncStateSerializer
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
private object BatterySyncStateSerializer : Serializer<BatterySyncConfig> {
    override val defaultValue = BatterySyncConfig(
        batterySyncEnabled = false,
        phoneChargeNotificationEnabled = false,
        phoneLowNotificationEnabled = false,
        phoneChargeThreshold = 90,
        phoneLowThreshold = 15,
        notificationPosted = false
    )

    override suspend fun readFrom(input: InputStream): BatterySyncConfig =
        ProtoBuf.decodeFromByteArray(input.readBytes())

    override suspend fun writeTo(t: BatterySyncConfig, output: OutputStream) =
        output.write(ProtoBuf.encodeToByteArray(t))
}

/**
 * A [BatterySyncConfigRepository] backed by a DataStore.
 */
class BatterySyncConfigDsRepository(context: Context) : BatterySyncConfigRepository {

    private val dataStore = context.batterySyncConfigStore

    override fun getBatterySyncState(): Flow<BatterySyncConfig> = dataStore.data

    override suspend fun updateBatterySyncState(block: (BatterySyncConfig) -> BatterySyncConfig) {
        dataStore.updateData(block)
    }
}
