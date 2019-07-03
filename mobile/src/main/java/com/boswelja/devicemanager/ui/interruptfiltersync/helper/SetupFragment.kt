package com.boswelja.devicemanager.ui.interruptfiltersync.helper

import android.content.ComponentName
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
import com.boswelja.devicemanager.service.InterruptFilterLocalChangeListener
import com.google.android.material.button.MaterialButton

internal class SetupFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_interrupt_filter_sync_helper_setup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<MaterialButton>(R.id.next_button)!!.apply {
            setOnClickListener {
                (activity as InterruptFilterSyncHelperActivity).checkWatchNotiAccess()
            }
        }

        view.findViewById<LinearLayout>(R.id.steps_holder)!!.also { stepsHolder ->
            val textViewPadding = Utils.complexTypeDp(resources, 8.0f).toInt()
            val steps = resources.getStringArray(R.array.interrupt_filter_sync_to_watch_steps)
            for (i in 0 until steps.size) {
                var stepText = "${i + 1}. ${steps[i]}"
                if (stepText.contains("%s")) {
                    stepText = stepText.format(getString(R.string.interrupt_filter_access_adb_command).format(ComponentName(context!!, InterruptFilterLocalChangeListener::class.java).flattenToString()))
                }
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

    fun setError() {

    }
}