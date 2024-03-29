package com.boswelja.smartwatchextensions.phonelocking

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest

/**
 * A [ComplicationDataSourceService] for phone locking.
 */
class LockPhoneComplicationProvider : ComplicationDataSourceService() {

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        val complicationData = createComplicationDataFor(request.complicationType)

        if (complicationData != null) {
            listener.onComplicationData(complicationData)
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createComplicationDataFor(type)
    }

    private fun createComplicationDataFor(type: ComplicationType): ComplicationData? {
        val intent = Intent(this, LockPhoneComplicationActivity::class.java).apply {
            action = LockPhone
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val icon = MonochromaticImage.Builder(
            Icon.createWithResource(this, R.drawable.complication_lock_phone)
        ).setAmbientImage(
            Icon.createWithResource(this, R.drawable.complication_lock_phone_ambient)
        ).build()
        val text = PlainComplicationText.Builder(getString(R.string.lock_phone_label)).build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(text, text)
                    .setTapAction(pendingIntent)
                    .setMonochromaticImage(icon)
                    .build()
            }
            else -> null
        }
    }
}
