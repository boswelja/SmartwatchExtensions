package com.boswelja.smartwatchextensions.batterysync.platform

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
import com.boswelja.smartwatchextensions.batterysync.R
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatteryStatsRepository
import com.boswelja.smartwatchextensions.batterysync.domain.repository.BatterySyncConfigRepository
import com.boswelja.smartwatchextensions.batterysync.getBatteryDrawableRes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.android.ext.android.inject

/**
 * A [ComplicationDataSourceService] for displaying info about the connected phone's battery.
 */
class PhoneBatteryComplicationProvider : SuspendingComplicationDataSourceService() {

    private val batterySyncConfigRepository: BatterySyncConfigRepository by inject()
    private val batteryStatsRepository: BatteryStatsRepository by inject()

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createComplicationDataFor(PREVIEW_PERCENT, type)
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val batterySyncEnabled = batterySyncConfigRepository.getBatterySyncState()
            .map { it.batterySyncEnabled }
            .first()
        val complicationData = if (batterySyncEnabled) {
            val batteryStats = batteryStatsRepository.getPhoneBatteryStats()
                .map { it.percent }
                .first()
            createComplicationDataFor(batteryStats, request.complicationType)
        } else {
            createDisabledComplicationData(request.complicationType)
        }

        return complicationData
    }

    /**
     * Create a [ComplicationData] for the given battery percent and type.
     * @param percent The battery percent to create [ComplicationData] for.
     * @param type The [ComplicationType] of the required [ComplicationData].
     * @return The created [ComplicationData], or null if type was invalid for this complication.
     */
    private fun createComplicationDataFor(percent: Int, type: ComplicationType): ComplicationData? {
        val text = if (percent > 0) {
            getString(R.string.battery_percent, percent.toString())
        } else {
            getString(R.string.battery_percent_unknown)
        }
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

    private fun createDisabledComplicationData(type: ComplicationType): ComplicationData? {
        val text = PlainComplicationText
            .Builder(getString(R.string.battery_sync_complication_disabled))
            .build()
        val icon = MonochromaticImage.Builder(
            Icon.createWithResource(this, getBatteryDrawableRes(-1))
        ).build()

        return when (type) {
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(0f, 0f, 1f, text)
                    .setMonochromaticImage(icon)
                    .setText(text)
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(text, text)
                    .setMonochromaticImage(icon)
                    .build()
            }
            else -> null
        }
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
