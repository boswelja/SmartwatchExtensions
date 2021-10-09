package com.boswelja.smartwatchextensions.batterysync

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

val Context.batteryStatsStore: DataStore<BatteryStats> by dataStore(
    "batterystats.pb",
    BatteryStatsStoreSerializer
)

@Suppress("BlockingMethodInNonBlockingContext")
private object BatteryStatsStoreSerializer : Serializer<BatteryStats> {
    override val defaultValue = BatteryStats()

    override suspend fun readFrom(input: InputStream): BatteryStats =
        BatteryStats.ADAPTER.decode(input)

    override suspend fun writeTo(t: BatteryStats, output: OutputStream) = t.encode(output)
}

actual class BatteryStatsStore(private val batteryStatsStore: DataStore<BatteryStats>) {
    actual fun getStatsForPhone(): Flow<BatteryStats> = batteryStatsStore.data
    actual suspend fun updateStatsForPhone(newStats: BatteryStats) {
        batteryStatsStore.updateData { newStats }
    }
}
