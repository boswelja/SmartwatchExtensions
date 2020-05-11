/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.TypedValue

object Utils {

    /**
     * Gets the app icon for a given package name.
     * @param context [Context].
     * @param packageName The name of the package to get an app icon for.
     * @param fallbackIcon The fallback icon to use in case an app icon can't be found.
     * @return The [Drawable] for the app icon.
     */
    fun getAppIcon(context: Context, packageName: String, fallbackIcon: Drawable? = null): Drawable {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (ignored: Exception) {
            fallbackIcon ?: context.getDrawable(R.drawable.ic_app_icon_unknown)!!
        }
    }

    /**
     * Gets a DiP value in pixels.
     * @param resources [Resources].
     * @param dp The DiP count to convert to pixels.
     */
    fun complexTypeDp(resources: Resources, dp: Float): Float =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}
