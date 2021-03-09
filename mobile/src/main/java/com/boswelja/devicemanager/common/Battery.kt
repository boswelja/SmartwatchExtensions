package com.boswelja.devicemanager.common

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

@PreviewParameter(PreviewProvider::class)
@Preview(showBackground = true)
@Composable
fun WatchBatteryIcon(
    percent: Int,
    modifier: Modifier = Modifier
) {
    val drawable = when (percent) {
        in 0..20 -> R.drawable.battery_alert
        in 20..30 -> R.drawable.battery_20
        in 30..40 -> R.drawable.battery_30
        in 40..50 -> R.drawable.battery_40
        in 50..60 -> R.drawable.battery_50
        in 60..70 -> R.drawable.battery_60
        in 70..80 -> R.drawable.battery_70
        in 80..90 -> R.drawable.battery_80
        in 90..100 -> R.drawable.battery_90
        100 -> R.drawable.battery_full
        else -> R.drawable.battery_unknown
    }
    Icons.Default.BatteryStd
    Icon(
        painterResource(drawable),
        null,
        modifier,
        tint = colorResource(id = R.color.iconTint)
    )
}

private class PreviewProvider : PreviewParameterProvider<Int> {
    override val values: Sequence<Int>
        get() = sequenceOf(0, 1, 11, 21, 31, 41, 51, 61, 71, 81, 91, 100)
}
