/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R

class ConfirmationFragment : Fragment() {

    private lateinit var buttonCallbacks: ButtonCallbacks
    private var header: TextView? = null
    private var description: TextView? = null
    private var confirm: ImageButton? = null
    private var cancel: ImageButton? = null
    private var descText: String = ""
    private var headerText: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        header = view.findViewById(R.id.heading)
        description = view.findViewById(R.id.description)
        if (!headerText.isEmpty()) {
            header?.text = headerText
        }
        if (!descText.isEmpty()) {
            description?.text = descText
        }

        confirm = view.findViewById(R.id.confirm)
        cancel = view.findViewById(R.id.cancel)
        confirm?.setOnClickListener {
            buttonCallbacks.onAccept()
        }
        cancel?.setOnClickListener {
            buttonCallbacks.onCancel()
        }
    }

    fun setButtonCallback(btnCallbacks: ButtonCallbacks) {
        buttonCallbacks = btnCallbacks
    }

    fun setHeaderText(string: String) {
        if (header != null) {
            header?.text = string
        } else {
            headerText = string
        }
    }

    fun setDescText(string: String) {
        if (description != null) {
            description?.text = string
        } else {
            descText = string
        }
    }

    interface ButtonCallbacks {

        fun onCancel()

        fun onAccept()
    }
}