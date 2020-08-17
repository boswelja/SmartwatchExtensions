/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter

@BindingAdapter("srcRes")
fun ImageView.setImageRes(@DrawableRes drawableRes: Int) {
    if (drawableRes != 0) setImageResource(drawableRes)
}
@BindingAdapter("srcDrawable")
fun ImageView.setDrawable(drawable: Drawable?) {
    setImageDrawable(drawable)
}
@BindingAdapter("srcBitmap")
fun ImageView.setBitmap(bitmap: Bitmap?) {
    setImageBitmap(bitmap)
}

@BindingAdapter("textRes")
fun TextView.setTextRes(@StringRes stringRes: Int) {
    if (stringRes != 0) setText(stringRes)
}
