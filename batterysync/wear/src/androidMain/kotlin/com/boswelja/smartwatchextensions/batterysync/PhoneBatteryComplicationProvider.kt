package com.boswelja.smartwatchextensions.batterysync

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.boswelja.smartwatchextensions.batterysync.common.getBatteryDrawableRes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.android.ext.android.inject

/**
 * A [ComplicationDataSourceService] for displaying info about the connected phone's battery.
 */
class PhoneBatteryComplicationProvider : SuspendingComplicationDataSourceService() {

    private val batteryStatsRepository: BatteryStatsRepository by inject()

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val batteryStats = batteryStatsRepository.getPhoneBatteryStats()
            .map { it.percent }
            .first()
        return createComplicationDataFor(batteryStats, request.complicationType)
    }

    /**
     * Create a [ComplicationData] for a complication with given data.
     * @param percent The battery percent to create [ComplicationData] for.
     * @param type The [ComplicationType] of the required [ComplicationData].
     * @return The created [ComplicationData], or null if type was invalid for this complication.
     */
    private fun createComplicationDataFor(percent: Int, type: ComplicationType): ComplicationData? {
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
                    RANGE_MIN,
                    RANGE_MAX,
                    complicationText
                )
                    .setText(complicationText)
                    .setMonochromaticImage(icon)
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(complicationText, complicationText)
                    .setMonochromaticImage(icon)
                    .build()
            }
            else -> null
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createComplicationDataFor(PREVIEW_PERCENT, type)
    }

    companion object {

        private const val PREVIEW_PERCENT = 66
        private const val RANGE_MIN = 0f
        private const val RANGE_MAX = 100f

        /**
         * Update all phone battery complications.
         */
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
