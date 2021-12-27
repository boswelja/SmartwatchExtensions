package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

/**
 * Gets a [DataStore] for storing [BatteryStats].
 */
val Context.batteryStatsStore: DataStore<BatteryStats> by dataStore(
    "batterystats.pb",
    BatteryStatsStoreSerializer
)

@Suppress("BlockingMethodInNonBlockingContext")
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
actual class BatteryStatsStore(private val batteryStatsStore: DataStore<BatteryStats>) {

    /**
     * Flow the connected phone's [BatteryStats].
     */
    actual fun getStatsForPhone(): Flow<BatteryStats> = batteryStatsStore.data

    /**
     * Update the connected phone's [BatteryStats].
     * @param newStats The new [BatteryStats] to store.
     */
    actual suspend fun updateStatsForPhone(newStats: BatteryStats) {
        batteryStatsStore.updateData { newStats }
    }
}
