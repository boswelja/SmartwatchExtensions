package com.boswelja.devicemanager.ui.interruptfiltersync.helper

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
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
        view.findViewById<AppCompatTextView>(R.id.adb_command_text)!!.apply {
            text = getString(R.string.interrupt_filter_access_adb_command).format(ComponentName(context!!, InterruptFilterLocalChangeListener::class.java).flattenToString())
        }
    }
}