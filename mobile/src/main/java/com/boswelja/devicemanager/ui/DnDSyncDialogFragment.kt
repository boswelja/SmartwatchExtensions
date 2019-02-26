/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
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

    private val listener = MessageClient.OnMessageReceivedListener {
        if (it.path == References.REQUEST_DND_ACCESS_STATUS) {
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

        messageClient = Wearable.getMessageClient(context!!)
        messages = view.findViewById(R.id.messages)
        loadingSpinner = view.findViewById(R.id.loading_spinner)

        cancelBtn = view.findViewById(R.id.cancel_btn)
        cancelBtn.setOnClickListener {
            dismiss()
        }
        confirmBtn = view.findViewById(R.id.confirm_button)
        confirmBtn.setOnClickListener {
            checkPermission()
        }
        shareBtn = view.findViewById(R.id.share_btn)
        shareBtn.setOnClickListener {
            Utils.shareText(context!!, getString(R.string.dnd_sync_adb_command))
        }
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
                            messageClient.sendMessage(node?.id!!, References.REQUEST_DND_ACCESS_STATUS, null)
                        }
                    }
                }
    }

    private fun onResponse(hasDnDAccess: Boolean) {
        if (hasDnDAccess) {
            (activity as MainActivity).createSnackbar("Permissions Granted!")
            dismiss()
        } else {
            setLoading(false)
            Toast.makeText(context!!, "Permission not granted, please try again", Toast.LENGTH_LONG).show()
        }
        responseListener?.onResponse(hasDnDAccess)
    }

    fun setResponseListener(responseListener: ResponseListener) {
        this.responseListener = responseListener
    }

    override fun dismiss() {
        super.dismiss()
        messageClient.removeListener(listener)
    }

    interface ResponseListener {
        fun onResponse(success: Boolean)
    }
}