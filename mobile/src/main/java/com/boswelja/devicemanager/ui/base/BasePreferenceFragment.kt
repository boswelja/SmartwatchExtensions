/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.base

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService

abstract class BasePreferenceFragment : PreferenceFragmentCompat() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var activity: BaseWatchPickerActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        activity = getActivity() as BaseWatchPickerActivity
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateRecyclerView(inflater: LayoutInflater?, parent: ViewGroup?, savedInstanceState: Bundle?): RecyclerView {
        val padding = Utils.complexTypeDp(resources, 8f)
        return super.onCreateRecyclerView(inflater, parent, savedInstanceState).apply {
            clipToPadding = false
            setPadding(0, padding.toInt(), 0, padding.toInt())
        }
    }

    fun getWatchConnectionManager(): WatchConnectionService? = activity.watchConnectionManager
}
