/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.changelog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber

class ChangelogDialogFragment : BottomSheetDialogFragment() {

    private lateinit var changelog: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate() called")
        changelog = processChangelog()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.bottom_sheet_changelog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated() called")
        setupRecyclerView(view)
    }

    /**
     * Set up the changelog [RecyclerView].
     * @param view The [View] containing the [RecyclerView].
     */
    private fun setupRecyclerView(view: View) {
        Timber.d("setupRecyclerView() called")
        view.findViewById<RecyclerView>(R.id.changelog_recyclerview).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = ChangelogAdapter(changelog)
        }
    }

    /**
     * Process the raw changelog to conform with any styles we set per line.
     * @return An [Array] of [String] objects representing the processed changelog.
     */
    private fun processChangelog(): Array<String> {
        Timber.d("processChangelog() called")
        val rawChangelog = context?.resources?.getStringArray(R.array.version_changelog)!!
        val bullet = context?.getString(R.string.changelog_change_prefix)!!
        val processedChangelog = ArrayList<String>()
        for (change in rawChangelog) {
            processedChangelog.add("$bullet $change")
        }
        return processedChangelog.toTypedArray()
    }

    /**
     * Show the [ChangelogDialogFragment].
     * @param fragmentManager The [FragmentManager] that will be used to handle the transition.
     */
    fun show(fragmentManager: FragmentManager) = show(fragmentManager, "ChangelogDialog")
}
