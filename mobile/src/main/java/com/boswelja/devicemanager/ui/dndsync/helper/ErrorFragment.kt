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
import timber.log.Timber

internal class ErrorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
            inflater.inflate(R.layout.fragment_dnd_sync_helper_error, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated() called")
        setupFinishButton()
    }

    /**
     * Set up the finish button.
     */
    private fun setupFinishButton() {
        Timber.d("setupFinishButton() called")
        view!!.findViewById<MaterialButton>(R.id.finish_button)!!.apply {
            setOnClickListener {
                activity?.finish()
            }
        }
    }

    /**
     * Show watch version incompatible error message.
     */
    fun showWatchVersionIncompatible() {
        Timber.d("showWatchVersionIncompatible() called")
        view!!.findViewById<AppCompatTextView>(R.id.watch_version_incompatible_text)!!
                .visibility = View.VISIBLE
    }

    /**
     * Show watch unreachable error.
     */
    fun showWatchUnreachable() {
        Timber.d("showWatchUnreachable() called")
        view!!.findViewById<AppCompatTextView>(R.id.watch_unreachable_text)!!
                .visibility = View.VISIBLE
    }
}
