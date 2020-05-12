package com.boswelja.devicemanager.common

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

object GooglePlayUtils {

    /**
     * Gets the Play Store URL for Wearable Extensions.
     * @return The URL as a [String].
     */
    fun getPlayStoreLink(context: Context?): String =
            "https://play.google.com/store/apps/details?id=${context?.packageName}"

    /**
     * Gets an [Intent] that launches the Google Play Store to the listing for
     * the package that provided the [Context].
     * @param context [Context].
     * @return The [Intent] to launch the Play Store with.
     */
    fun getPlayStoreIntent(context: Context?): Intent =
            Intent(Intent.ACTION_VIEW).apply {
                data = getPlayStoreLink(context).toUri()
                setPackage("com.android.vending")
            }
}