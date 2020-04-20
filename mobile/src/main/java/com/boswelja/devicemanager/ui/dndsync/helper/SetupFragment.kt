/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
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
import com.boswelja.devicemanager.Utils
import com.google.android.material.button.MaterialButton
import timber.log.Timber

internal class SetupFragment : Fragment() {

    private var errorMessage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
            inflater.inflate(R.layout.fragment_dnd_sync_helper_setup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCrated() called")
        setupNextButton()
        setupStepsHolder()
    }

    /**
     * Set up the next button.
     */
    private fun setupNextButton() {
        Timber.d("setupNextButton() called")
        view!!.findViewById<MaterialButton>(R.id.next_button)!!.apply {
            setOnClickListener {
                (activity as DnDSyncHelperActivity).checkWatchNotiAccess()
            }
        }
    }

    /**
     * Sets up the steps list, and shows [errorMessage] if it's not null or empty.
     */
    private fun setupStepsHolder() {
        Timber.d("setupStepsHolder() called")
        view!!.findViewById<LinearLayout>(R.id.steps_holder)!!.also { stepsHolder ->
            val textViewPadding = Utils.complexTypeDp(resources, 4.0f).toInt()
            if (!errorMessage.isNullOrEmpty()) {
                Timber.i("Showing error message")
                createTextView(textViewPadding, errorMessage!!).apply {
                    setTextColor(Color.RED)
                }.also { textView ->
                    stepsHolder.addView(textView)
                }
            }

            val steps = processSteps()
            steps.forEach {
                createTextView(textViewPadding, it).also { textView ->
                    stepsHolder.addView(textView)
                }
            }
        }
    }

    /**
     * Process the list of steps into something better formatted for our needs.
     * @return An [Array] of [String] objects representing the steps we need.
     */
    private fun processSteps(): Array<String> {
        Timber.d("processSteps() called")
        val steps = resources.getStringArray(R.array.interrupt_filter_sync_to_watch_steps)
        return steps.mapIndexed { index, step -> "${index + 1}. $step" }.toTypedArray()
    }

    /**
     * Creates a new [AppCompatTextView].
     * @param verticalPadding The top and bottom padding values for the new [AppCompatTextView].
     * @param textString The text for the new [AppCompatTextView].
     * @return The created [AppCompatTextView].
     */
    private fun createTextView(verticalPadding: Int, textString: String): AppCompatTextView {
        Timber.d("createTextView() called")
        return AppCompatTextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            text = textString
            textSize = 16.0f
            setTextIsSelectable(true)
            setPadding(0, verticalPadding, 0, verticalPadding)
        }
    }

    /**
     * Sets an error message to show if needed.
     * @param message The error message to show.
     */
    fun setErrorMessage(message: String) {
        Timber.d("setErrorMessage() called")
        errorMessage = message
    }
}
