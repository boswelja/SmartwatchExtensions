/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.base

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.watchmanager.WatchManager

/**
 * A [PreferenceFragmentCompat] that automatically adjusts it's [RecyclerView] properties
 * to fit the app theme better, and also automatically initializes a [SharedPreferences] instance.
 */
abstract class BaseWatchPickerPreferenceFragment : BasePreferenceFragment() {

    lateinit var activity: BaseWatchPickerActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as BaseWatchPickerActivity
    }

    /**
     * Get an instance of [WatchManager] from the parent activity.
     */
    fun getWatchConnectionManager(): WatchManager? = activity.watchManager
}
