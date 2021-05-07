package com.boswelja.smartwatchextensions.phonelocking

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.complications.ComplicationProviderService
import androidx.wear.complications.ComplicationRequest
import androidx.wear.complications.data.ComplicationData
import androidx.wear.complications.data.ComplicationType
import androidx.wear.complications.data.MonochromaticImage
import androidx.wear.complications.data.PlainComplicationText
import androidx.wear.complications.data.ShortTextComplicationData
import com.boswelja.smartwatchextensions.ActionServiceStarter
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.connection.Messages.LOCK_PHONE
import timber.log.Timber

class LockPhoneComplicationProvider : ComplicationProviderService() {

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        val complicationData = createComplicationDataFor(request.complicationType)

        if (complicationData != null) {
            listener.onComplicationData(complicationData)
        } else {
            Timber.w("Complication type ${request.complicationType} invalid")
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createComplicationDataFor(type)
    }

    private fun createComplicationDataFor(type: ComplicationType): ComplicationData? {
        val intent = Intent(this, ActionServiceStarter::class.java).apply {
            action = LOCK_PHONE
        }
        val pendingIntent = PendingIntent.getBroadcast(
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
