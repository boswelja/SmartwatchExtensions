package com.boswelja.smartwatchextensions.batterysync

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.complications.data.ComplicationData
import androidx.wear.complications.data.ComplicationType
import androidx.wear.complications.data.MonochromaticImage
import androidx.wear.complications.data.PlainComplicationText
import androidx.wear.complications.data.RangedValueComplicationData
import androidx.wear.complications.data.ShortTextComplicationData
import androidx.wear.complications.datasource.ComplicationDataSourceService
import androidx.wear.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.complications.datasource.ComplicationRequest
import com.boswelja.smartwatchextensions.ActionsActivity
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.batterysync.common.getBatteryDrawableRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A [ComplicationDataSourceService] for displaying info about the connected phone's battery.
 */
class PhoneBatteryComplicationProvider : ComplicationDataSourceService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        coroutineScope.launch {
            val batteryStats = BatteryStatsStore(batteryStatsStore).getStatsForPhone()
                .map { it.percent }
                .first()
            val complicationData = createComplicationDataFor(batteryStats, request.complicationType)

            if (complicationData != null) {
                listener.onComplicationData(complicationData)
            } else {
                Timber.w("Complication type ${request.complicationType} invalid")
            }
        }
    }

    /**
     * Create a [ComplicationData] for a complication with given data.
     * @param percent The battery percent to create [ComplicationData] for.
     * @param type The [ComplicationType] of the required [ComplicationData].
     * @return The created [ComplicationData], or null if type was invalid for this complication.
     */
    private fun createComplicationDataFor(percent: Int, type: ComplicationType): ComplicationData? {
        val refreshDataIntent =
            Intent(this, ActionsActivity::class.java).apply {
                action = REQUEST_BATTERY_UPDATE_PATH
            }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, refreshDataIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val text =
            if (percent > 0) String.format(getString(R.string.battery_percent), percent)
            else getString(R.string.battery_percent_unknown)
        val icon = MonochromaticImage.Builder(
            Icon.createWithResource(this, getBatteryDrawableRes(percent))
        ).build()

        val complicationText = PlainComplicationText.Builder(text).build()
        return when (type) {
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    percent.toFloat(),
                    0f,
                    100f,
                    complicationText
                )
                    .setText(complicationText)
                    .setTapAction(pendingIntent)
                    .setMonochromaticImage(icon)
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(complicationText, complicationText)
                    .setMonochromaticImage(icon)
                    .setTapAction(pendingIntent)
                    .build()
            }
            else -> null
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createComplicationDataFor(66, type)
    }

    companion object {
        fun updateAll(context: Context) {
            ComplicationDataSourceUpdateRequester.create(
                context,
                ComponentName(
                    context.packageName, PhoneBatteryComplicationProvider::class.java.name
                )
            ).requestUpdateAll()
        }
    }
}
