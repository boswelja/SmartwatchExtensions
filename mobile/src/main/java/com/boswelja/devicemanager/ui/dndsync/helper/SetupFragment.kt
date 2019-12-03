/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.dndsync.helper

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Utils
import com.google.android.material.button.MaterialButton

internal class SetupFragment : Fragment() {

    private var errorMessage: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_interrupt_filter_sync_helper_setup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<MaterialButton>(R.id.next_button)!!.apply {
            setOnClickListener {
                (activity as DnDSyncHelperActivity).checkWatchNotiAccess()
            }
        }

        view.findViewById<LinearLayout>(R.id.steps_holder)!!.also { stepsHolder ->
            val textViewPadding = Utils.complexTypeDp(resources, 4.0f).toInt()
            if (errorMessage != null) {
                createTextView(textViewPadding, errorMessage!!).apply {
                    setTextColor(Color.RED)
                }.also { textView ->
                    stepsHolder.addView(textView)
                }
            }
            val steps = resources.getStringArray(R.array.interrupt_filter_sync_to_watch_steps)
            for (i in steps.indices) {
                val stepText = "${i + 1}. ${steps[i]}"
                createTextView(textViewPadding, stepText).also { textView ->
                    stepsHolder.addView(textView)
                }
            }
        }
    }

    private fun createTextView(viewPadding: Int, textString: String): AppCompatTextView {
        return AppCompatTextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            text = textString
            textSize = 16.0f
            setTextIsSelectable(true)
            setPadding(0, viewPadding, 0, viewPadding)
        }
    }

    fun setErrorMessage(message: String) {
        errorMessage = message
    }
}
