/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.interruptfiltersync

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.Extensions.fromByteArray
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.service.DnDLocalChangeListener
import com.boswelja.devicemanager.ui.base.BaseDialogFragment
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class InterruptFilterSyncHelperDialog : BaseDialogFragment() {

    private lateinit var message: TextView
    private lateinit var loadingSpinner: ProgressBar

    private lateinit var cancelBtn: Button
    private lateinit var confirmBtn: Button
    private lateinit var shareBtn: Button

    private lateinit var messageClient: MessageClient

    private var responseListener: ResponseListener? = null

    private var isInitialCheck = true

    private val listener = MessageClient.OnMessageReceivedListener {
        if (it.path == REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH) {
            val hasDnDAccess = Boolean.fromByteArray(it.data)
            onResponse(hasDnDAccess)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_interrupt_filter_sync, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        message = view.findViewById(R.id.message)
        loadingSpinner = view.findViewById(R.id.loading_spinner)
        cancelBtn = view.findViewById(R.id.cancel_btn)
        confirmBtn = view.findViewById(R.id.confirm_button)
        shareBtn = view.findViewById(R.id.share_btn)

        val adbCommand = getString(R.string.interrupt_filter_access_adb_command).format(ComponentName(context!!, DnDLocalChangeListener::class.java).flattenToString())
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

        checkPermission()
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            message.visibility = View.GONE
            loadingSpinner.visibility = View.VISIBLE
        } else {
            message.visibility = View.VISIBLE
            loadingSpinner.visibility = View.GONE
        }
        confirmBtn.isEnabled = !loading
        shareBtn.isEnabled = !loading
    }

    private fun checkPermission() {
        setLoading(true)

        messageClient.addListener(listener)

        Wearable.getCapabilityClient(context!!)
                .getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE)
                .addOnCompleteListener {
                    try {
                        if (it.isSuccessful && it.result != null) {
                            val nodes = it.result!!.nodes
                            if (nodes.isNullOrEmpty()) {
                                createSnackbar(getString(R.string.no_watch_found))
                                dismiss()
                            } else {
                                messageClient.sendMessage(nodes.first().id!!, REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH, null)
                            }
                        } else {
                            createSnackbar(getString(R.string.no_watch_found))
                            dismiss()
                        }
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }
                }
    }

    private fun onResponse(hasDnDAccess: Boolean) {
        if (hasDnDAccess) {
            dismiss()
            if (!isInitialCheck) {
                createSnackbar(getString(R.string.interrupt_filter_sync_to_watch_perm_granted_message))
                isInitialCheck = false
            }
        } else {
            setLoading(false)
            if (!isInitialCheck) {
                Toast.makeText(context!!, getString(R.string.interrupt_filter_sync_to_watch_perm_denied_message), Toast.LENGTH_LONG).show()
                isInitialCheck = false
            }
        }
        messageClient.removeListener(listener)
        responseListener?.onResponse(hasDnDAccess)
    }

    fun setResponseListener(responseListener: ResponseListener) {
        this.responseListener = responseListener
    }

    private fun createSnackbar(message: String) {
        if (activity != null) {
            (activity as InterruptFilterSyncPreferenceActivity).createSnackBar(message)
        }
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
