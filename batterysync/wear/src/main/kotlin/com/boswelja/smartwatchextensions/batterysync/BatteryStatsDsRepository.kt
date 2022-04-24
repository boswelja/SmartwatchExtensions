package com.boswelja.smartwatchextensions.batterysync

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

private val Context.batteryStatsStore: DataStore<BatteryStats> by dataStore(
    "batterystats.pb",
    BatteryStatsStoreSerializer
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
private object BatteryStatsStoreSerializer : Serializer<BatteryStats> {
    override val defaultValue = BatteryStats(0, false, 0)

    override suspend fun readFrom(input: InputStream): BatteryStats =
        ProtoBuf.decodeFromByteArray(input.readBytes())

    override suspend fun writeTo(t: BatteryStats, output: OutputStream) =
        output.write(ProtoBuf.encodeToByteArray(t))
}

/**
 * A class for storing and updating the battery stats of the connected phone.
 */
class BatteryStatsDsRepository(context: Context): BatteryStatsRepository {

    private val dataStore: DataStore<BatteryStats> = context.batteryStatsStore

    override fun getPhoneBatteryStats(): Flow<BatteryStats> = dataStore.data

    override suspend fun updatePhoneBatteryStats(newStats: BatteryStats) {
        dataStore.updateData { newStats }
    }
}
