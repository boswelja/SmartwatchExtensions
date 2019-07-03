package com.boswelja.devicemanager.ui.interruptfiltersync.helper

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
                (activity as InterruptFilterSyncHelperActivity).checkWatchNotiAccess()
            }
        }

        view.findViewById<LinearLayout>(R.id.steps_holder)!!.also { stepsHolder ->
            val textViewPadding = Utils.complexTypeDp(resources, 4.0f).toInt()
            if (errorMessage != null) {
                AppCompatTextView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                    text = errorMessage
                    textSize = 16.0f
                    setTextIsSelectable(true)
                    setPadding(0, textViewPadding, 0, textViewPadding)
                    setTextColor(Color.RED)
                }.also { textView ->
                    stepsHolder.addView(textView)
                }
            }
            val steps = resources.getStringArray(R.array.interrupt_filter_sync_to_watch_steps)
            for (i in 0 until steps.size) {
                val stepText = "${i + 1}. ${steps[i]}"
                AppCompatTextView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                    text = stepText
                    textSize = 16.0f
                    setTextIsSelectable(true)
                    setPadding(0, textViewPadding, 0, textViewPadding)
                }.also { textView ->
                    stepsHolder.addView(textView)
                }
            }
        }
    }

    fun setErrorMessage(message: String) {
        errorMessage = message
    }
}