/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.common.recyclerview

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.CurvingLayoutCallback
import com.boswelja.devicemanager.R

/**
 * A slightly custom implementation of [CurvingLayoutCallback] that ignores
 * [R.layout.common_recyclerview_item_separator] views.
 */
class CustomCurvingLayoutCallback(context: Context) : CurvingLayoutCallback(context) {

    override fun onLayoutFinished(child: View?, parent: RecyclerView?) {
        if (child?.id != R.id.divider) {
            super.onLayoutFinished(child, parent)
        }
    }
}
