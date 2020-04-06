/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.dndsync.helper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.google.android.material.button.MaterialButton

internal class ErrorFragment : Fragment() {

    var watchVersionIncompatible: Boolean = false
    var watchUnreachable: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_dnd_sync_helper_error, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (watchVersionIncompatible) {
            view.findViewById<AppCompatTextView>(R.id.watch_version_incompatible_text)!!.visibility = View.VISIBLE
        }
        if (watchUnreachable) {
            view.findViewById<AppCompatTextView>(R.id.watch_unreachable_text)!!.visibility = View.VISIBLE
        }
        view.findViewById<MaterialButton>(R.id.finish_button)!!.apply {
            setOnClickListener {
                activity?.finish()
            }
        }
    }
}
