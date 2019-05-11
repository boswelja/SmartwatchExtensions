/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.Utils

abstract class BasePreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreateRecyclerView(inflater: LayoutInflater?, parent: ViewGroup?, savedInstanceState: Bundle?): RecyclerView {
        val padding = Utils.complexTypeDp(resources, 8f)
        return super.onCreateRecyclerView(inflater, parent, savedInstanceState).apply {
            clipToPadding = false
            setPadding(0, padding.toInt(), 0, padding.toInt())
        }
    }
}
