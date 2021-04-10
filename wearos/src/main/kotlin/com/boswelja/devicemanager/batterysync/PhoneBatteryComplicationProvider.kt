package com.boswelja.devicemanager.batterysync

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.wear.complications.ComplicationProviderService
import androidx.wear.complications.ProviderUpdateRequester
import androidx.wear.complications.data.ComplicationData
import androidx.wear.complications.data.ComplicationType
import androidx.wear.complications.data.MonochromaticImage
import androidx.wear.complications.data.PlainComplicationText
import androidx.wear.complications.data.RangedValueComplicationData
import androidx.wear.complications.data.ShortTextComplicationData
import com.boswelja.devicemanager.ActionServiceStarter
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.devicemanager.phoneStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A [ComplicationProviderService] for displaying info about the connected phone's battery.
 */
class PhoneBatteryComplicationProvider : ComplicationProviderService() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onComplicationUpdate(
        complicationId: Int,
        type: ComplicationType,
        resultCallback: ComplicationUpdateListener
    ) {
        coroutineScope.launch {
            phoneStateStore.data.map { it.batteryPercent }.collect {
                val complicationData = createComplicationDataFor(it, type)

                if (complicationData != null) {
                    resultCallback.onUpdateComplication(complicationData)
                } else {
                    Timber.w("Complication type $type invalid")
                }
            }
        }
    }

    private fun createIcon(batteryPercent: Int): Icon {
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_phone_battery)!!
        drawable.level = batteryPercent
        return Icon.createWithBitmap(drawable.toBitmap())
    }

    /**
     * Create a [ComplicationData] for a complication with given data.
     * @param percent The battery percent to create [ComplicationData] for.
     * @param type The [ComplicationType] of the required [ComplicationData].
     * @return The created [ComplicationData], or null if type was invalid for this complication.
     */
    private fun createComplicationDataFor(percent: Int, type: ComplicationType): ComplicationData? {
        val refreshDataIntent =
            Intent(this, ActionServiceStarter::class.java).apply {
                action = REQUEST_BATTERY_UPDATE_PATH
            }
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, refreshDataIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val text =
            if (percent > 0) String.format(getString(R.string.battery_percent), percent)
            else getString(R.string.battery_percent_unknown)
        val icon = MonochromaticImage.Builder(createIcon(percent)).build()

        val complicationText = PlainComplicationText.Builder(text).build()
        return when (type) {
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(percent.toFloat(), 0f, 100f)
                    .setText(complicationText)
                    .setTapAction(pendingIntent)
                    .setMonochromaticImage(icon)
                    .setContentDescription(complicationText)
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(complicationText)
                    .setContentDescription(complicationText)
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
            ProviderUpdateRequester(
                context,
                ComponentName(
                    context.packageName, PhoneBatteryComplicationProvider::class.java.name
                )
            ).requestUpdateAll()
        }
    }
}
