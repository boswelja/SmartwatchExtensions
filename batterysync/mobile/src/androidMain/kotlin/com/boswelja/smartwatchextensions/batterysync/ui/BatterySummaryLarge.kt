package com.boswelja.smartwatchextensions.batterysync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.boswelja.smartwatchextensions.batterysync.BatteryStats
import com.boswelja.smartwatchextensions.batterysync.R
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * A Composable for displaying a large summary of a [BatteryStats].
 * @param modifier [Modifier].
 * @param batteryStats The battery stats to show info for.
 */
@Composable
fun BatterySummaryLarge(
    modifier: Modifier = Modifier,
    batteryStats: BatteryStats
) {

    val dataAgeMinutes by timestampToAge(
        timestamp = batteryStats.timestamp,
        ageUnit = TimeUnit.MINUTES
    )
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.battery_percent, batteryStats.percent.toString()),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = if (dataAgeMinutes > 1)
                    LocalContext.current.resources.getQuantityString(
                        R.plurals.battery_sync_last_updated_minutes,
                        dataAgeMinutes.toInt(),
                        dataAgeMinutes
                    )
                else stringResource(R.string.battery_sync_last_update_recent),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.fillMaxWidth()
            )
        }

        BatteryIcon(
            percent = batteryStats.percent,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
        )
    }
}

/**
 * Convert a timestamp to a human readable age value.
 * @param timestamp The timestamp in milliseconds to convert.
 * @param ageUnit The [TimeUnit] to display the age in.
 */
@Composable
fun timestampToAge(timestamp: Long, ageUnit: TimeUnit) = produceState(
    initialValue = ageUnit.convert(System.currentTimeMillis() - timestamp, TimeUnit.MILLISECONDS)
) {
    while (true) {
        delay(ageUnit.toMillis(1))
        value = ageUnit.convert(System.currentTimeMillis() - timestamp, TimeUnit.MILLISECONDS)
    }
}
