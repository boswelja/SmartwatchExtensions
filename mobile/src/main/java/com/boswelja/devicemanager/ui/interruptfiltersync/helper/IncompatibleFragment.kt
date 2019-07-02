package com.boswelja.devicemanager.ui.interruptfiltersync.helper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R

internal class IncompatibleFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_interrupt_filter_sync_helper_incompatible, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }
}