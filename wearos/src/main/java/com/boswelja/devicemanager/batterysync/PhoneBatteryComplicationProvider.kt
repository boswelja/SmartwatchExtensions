package com.boswelja.devicemanager.batterysync

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import androidx.wear.complications.ComplicationProviderService
import androidx.wear.complications.ProviderUpdateRequester
import androidx.wear.complications.data.ComplicationData
import androidx.wear.complications.data.ComplicationText
import androidx.wear.complications.data.ComplicationType
import androidx.wear.complications.data.MonochromaticImage
import androidx.wear.complications.data.RangedValueComplicationData
import androidx.wear.complications.data.ShortTextComplicationData
import com.boswelja.devicemanager.ActionServiceStarter
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.devicemanager.common.preference.PreferenceKey
import timber.log.Timber

/**
 * A [ComplicationProviderService] for displaying info about the connected phone's battery.
 */
class PhoneBatteryComplicationProvider : ComplicationProviderService() {

    override fun onComplicationUpdate(
        complicationId: Int,
        type: ComplicationType,
        resultCallback: ComplicationUpdateCallback
    ) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val percent = prefs.getInt(PreferenceKey.BATTERY_PERCENT_KEY, 0)

        val complicationData = createComplicationDataFor(percent, type)

        if (complicationData != null) {
            resultCallback.onUpdateComplication(complicationData)
        } else {
            Timber.w("Complication type $type invalid")
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

        return when (type) {
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(percent.toFloat(), 0f, 100f)
                    .setText(ComplicationText.plain(text))
                    .setTapAction(pendingIntent)
                    .setMonochromaticImage(icon)
                    .setContentDescription(ComplicationText.plain(text))
                    .build()
            }
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(ComplicationText.plain(text))
                    .setContentDescription(ComplicationText.plain(text))
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
