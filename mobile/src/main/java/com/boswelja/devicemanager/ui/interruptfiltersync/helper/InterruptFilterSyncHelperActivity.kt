/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.interruptfiltersync.helper

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Extensions.fromByteArray
import com.boswelja.devicemanager.common.interruptfiltersync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.common.interruptfiltersync.References.REQUEST_SDK_INT_PATH
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.ui.base.LoadingFragment
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import java.math.BigInteger

class InterruptFilterSyncHelperActivity : BaseToolbarActivity() {

    private var messageClient: MessageClient? = null

    private val loadingFragment: LoadingFragment = LoadingFragment()
    private var errorFragment: ErrorFragment? = null
    private var setupFragment: SetupFragment? = null

    private val messageListener = MessageClient.OnMessageReceivedListener {
        when (it.path) {
            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH -> {
                val hasNotiPolicyAccess = Boolean.fromByteArray(it.data)
                if (hasNotiPolicyAccess) {
                    showAllSetFragment()
                } else {
                    showSetupFragment()
                }
            }
            REQUEST_SDK_INT_PATH -> {
                val sdkInt = BigInteger(it.data).toInt()
                if (sdkInt > Build.VERSION_CODES.O) {
                    showWatchVersionError()
                } else {
                    checkWatchNotiAccess(false)
                }
            }
        }
    }

    override fun getContentViewId(): Int = R.layout.activity_interrupt_filter_sync_helper

    override fun onCreate(savedInstanceState: Bundle?) {
        setResult(RESULT_USER_DISMISSED)
        super.onCreate(savedInstanceState)
        if (!androidVersionCompatible()) {
            showPhoneVersionIncompatible()
        } else {
            init()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        messageClient?.removeListener(messageListener)
    }

    private fun init() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        messageClient = Wearable.getMessageClient(this)
        messageClient!!.addListener(messageListener)
        checkWatchSystemVersion()
    }

    private fun androidVersionCompatible(): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    private fun showLoading(animate: Boolean, reverse: Boolean = false) {
        changeFragment(loadingFragment, animate = animate, reverse = reverse)
    }

    private fun showWatchVersionError() {
        if (errorFragment == null) errorFragment = ErrorFragment()
        errorFragment!!.watchVersionIncompatible = true
        showErrorFragment()
    }

    private fun showPhoneVersionIncompatible() {
        if (errorFragment == null) errorFragment = ErrorFragment()
        showErrorFragment()
    }

    private fun showWatchNullError() {
        if (errorFragment == null) errorFragment = ErrorFragment()
        errorFragment!!.watchUnreachable = true
        showErrorFragment()
    }

    private fun showErrorFragment() {
        setResult(RESULT_FAILED)
        changeFragment(errorFragment!!)
    }

    private fun showAllSetFragment() {
        setResult(RESULT_OK)
        changeFragment(AllSetFragment())
    }

    private fun showSetupFragment() {
        if (setupFragment == null) {
            setupFragment = SetupFragment()
            changeFragment(setupFragment!!)
        } else {
            setupFragment!!.setErrorMessage("Permission not granted, try again")
            changeFragment(setupFragment!!, reverse = true)
        }
    }

    private fun changeFragment(fragment: Fragment, animate: Boolean = true, reverse: Boolean = false) {
        supportFragmentManager.beginTransaction().apply {
            if (animate) {
                if (reverse) {
                    setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                } else {
                    setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
            replace(R.id.fragment_holder, fragment)
        }.also {
            try {
                it.commit()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    private fun checkWatchSystemVersion() {
        showLoading(animate = false)
        if (!connectedWatchId.isNullOrEmpty()) {
            messageClient!!.sendMessage(connectedWatchId!!, REQUEST_SDK_INT_PATH, null)
        } else {
            showWatchNullError()
        }
    }

    fun checkWatchNotiAccess(animate: Boolean = true) {
        showLoading(animate = animate)
        if (!connectedWatchId.isNullOrEmpty()) {
            messageClient!!.sendMessage(connectedWatchId!!, REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH, null)
        } else {
            showWatchNullError()
        }
    }

    companion object {
        const val RESULT_OK = 0
        const val RESULT_FAILED = 1
        const val RESULT_USER_DISMISSED = 2
    }
}
