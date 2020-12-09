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
import androidx.databinding.BindingAdapter

@BindingAdapter("srcDrawable")
fun ImageView.setDrawable(drawable: Drawable?) {
    setImageDrawable(drawable)
}

@BindingAdapter("srcBitmap")
fun ImageView.setBitmap(bitmap: Bitmap?) {
    setImageBitmap(bitmap)
}

@BindingAdapter("imageLevel")
fun ImageView.setLevel(level: Int) {
    setImageLevel(level)
}
