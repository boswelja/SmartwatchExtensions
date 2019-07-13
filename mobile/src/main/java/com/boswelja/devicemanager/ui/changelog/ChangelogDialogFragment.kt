/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChangelogDialogFragment : BottomSheetDialogFragment() {

    private lateinit var changelog: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        val rawChangelog = context?.resources?.getStringArray(R.array.version_changelog)!!
        val bullet = context?.getString(R.string.bullet)!!
        val processedChangelog = ArrayList<String>()
        for (change in rawChangelog) {
            processedChangelog.add("$bullet $change")
        }
        changelog = processedChangelog.toTypedArray()

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.bottom_sheet_changelog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view as RecyclerView).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = ChangelogAdapter(changelog)
        }
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return AlertDialog.Builder(context!!)
//                .setPositiveButton(R.string.dialog_button_close) { dialog, _ -> dialog.dismiss() }
//                .setItems(changelog, null)
//                .setTitle(R.string.dialog_changelog_title)
//                .create()
//    }
}
