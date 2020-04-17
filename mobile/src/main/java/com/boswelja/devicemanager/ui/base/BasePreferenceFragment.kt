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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import timber.log.Timber

/**
 * A [PreferenceFragmentCompat] that automatically adjusts it's [RecyclerView] properties
 * to fit the app theme better, and also automatically initializes a [SharedPreferences] instance.
 */
abstract class BasePreferenceFragment : PreferenceFragmentCompat() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var activity: BaseWatchPickerActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate() called")
        activity = getActivity() as BaseWatchPickerActivity
        sharedPreferences = preferenceManager.sharedPreferences
    }

    override fun onCreateRecyclerView(inflater: LayoutInflater?, parent: ViewGroup?, savedInstanceState: Bundle?): RecyclerView {
        Timber.i("onCreateRecyclerView() called")
        val padding = resources.getDimension(R.dimen.recyclerview_vertical_padding)
        return super.onCreateRecyclerView(inflater, parent, savedInstanceState).apply {
            clipToPadding = false
            setPadding(0, padding.toInt(), 0, padding.toInt())
        }
    }

    /**
     * Get an instance of [WatchConnectionService] from the parent activity.
     */
    fun getWatchConnectionManager(): WatchConnectionService? = activity.watchConnectionManager
}
