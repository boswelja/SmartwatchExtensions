/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.content.ComponentName
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.DnDLocalChangeListener
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class DnDSyncDialogFragment : DialogFragment() {

    private lateinit var messages: View
    private lateinit var loadingSpinner: ProgressBar

    private lateinit var cancelBtn: Button
    private lateinit var confirmBtn: Button
    private lateinit var shareBtn: Button

    private lateinit var messageClient: MessageClient

    private var responseListener: ResponseListener? = null

    private var isInitialCheck = true

    private val listener = MessageClient.OnMessageReceivedListener {
        if (it.path == References.REQUEST_DND_ACCESS_STATUS_PATH) {
            val hasDnDAccess = it.data[0].toInt() == 1
            onResponse(hasDnDAccess)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_dnd_sync, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messages = view.findViewById(R.id.messages)
        loadingSpinner = view.findViewById(R.id.loading_spinner)
        cancelBtn = view.findViewById(R.id.cancel_btn)
        confirmBtn = view.findViewById(R.id.confirm_button)
        shareBtn = view.findViewById(R.id.share_btn)

        val adbCommand = getString(R.string.dnd_sync_adb_command).format(ComponentName(context!!, DnDLocalChangeListener::class.java).flattenToString())
        messageClient = Wearable.getMessageClient(context!!)

        cancelBtn.setOnClickListener {
            dismiss()
        }
        confirmBtn.setOnClickListener {
            checkPermission()
        }
        shareBtn.setOnClickListener {
            Utils.shareText(context!!, adbCommand)
        }

        val adbCommandTextView = view.findViewById<AppCompatTextView>(R.id.command_string)
        adbCommandTextView.text = adbCommand

        checkPermission()
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            messages.visibility = View.GONE
            loadingSpinner.visibility = View.VISIBLE
        } else {
            messages.visibility = View.VISIBLE
            loadingSpinner.visibility = View.GONE
        }
        confirmBtn.isEnabled = !loading
        shareBtn.isEnabled = !loading
    }

    private fun checkPermission() {
        setLoading(true)

        messageClient.addListener(listener)

        Wearable.getCapabilityClient(context!!)
                .getCapability(References.CAPABILITY_APP, CapabilityClient.FILTER_REACHABLE)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (node in it.result?.nodes!!) {
                            messageClient.sendMessage(node?.id!!, References.REQUEST_DND_ACCESS_STATUS_PATH, null)
                        }
                    }
                }
    }

    private fun onResponse(hasDnDAccess: Boolean) {
        if (hasDnDAccess) {
            dismiss()
            if (!isInitialCheck) {
                (activity as MainActivity).createSnackbar(getString(R.string.dnd_sync_watch_permission_granted))
                isInitialCheck = false
            }
        } else {
            setLoading(false)
            if (!isInitialCheck) {
                Toast.makeText(context!!, getString(R.string.dnd_sync_watch_permission_not_granted), Toast.LENGTH_LONG).show()
                isInitialCheck = false
            }
        }
        messageClient.removeListener(listener)
        responseListener?.onResponse(hasDnDAccess)
    }

    fun setResponseListener(responseListener: ResponseListener) {
        this.responseListener = responseListener
    }

    override fun dismiss() {
        super.dismiss()
        isInitialCheck = true
        messageClient.removeListener(listener)
    }

    interface ResponseListener {
        fun onResponse(success: Boolean)
    }
}