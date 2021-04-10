package com.boswelja.devicemanager.phonelocking

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.complications.ComplicationProviderService
import androidx.wear.complications.data.ComplicationData
import androidx.wear.complications.data.ComplicationType
import androidx.wear.complications.data.MonochromaticImage
import androidx.wear.complications.data.PlainComplicationText
import androidx.wear.complications.data.ShortTextComplicationData
import com.boswelja.devicemanager.ActionServiceStarter
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.connection.Messages.LOCK_PHONE
import timber.log.Timber

class LockPhoneComplicationProvider : ComplicationProviderService() {

    override fun onComplicationUpdate(
        complicationId: Int,
        type: ComplicationType,
        resultCallback: ComplicationUpdateListener
    ) {
        val complicationData = createComplicationDataFor(type)

        if (complicationData != null) {
            resultCallback.onUpdateComplication(complicationData)
        } else {
            Timber.w("Complication type $type invalid")
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return createComplicationDataFor(type)
    }

    private fun createComplicationDataFor(type: ComplicationType): ComplicationData? {
        val intent = Intent(this, ActionServiceStarter::class.java)
            .apply { action = LOCK_PHONE }
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val icon = MonochromaticImage.Builder(
            Icon.createWithResource(this, R.drawable.ic_phone_lock)
        ).build()
        val text = PlainComplicationText.Builder(getString(R.string.lock_phone_label)).build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(text)
                    .setTapAction(pendingIntent)
                    .setContentDescription(text)
                    .setMonochromaticImage(icon)
                    .build()
            }
            else -> null
        }
    }
}
