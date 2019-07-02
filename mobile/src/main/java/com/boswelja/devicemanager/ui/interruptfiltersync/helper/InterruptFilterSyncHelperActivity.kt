package com.boswelja.devicemanager.ui.interruptfiltersync.helper

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Extensions.fromByteArray
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.interruptfiltersync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class InterruptFilterSyncHelperActivity : BaseToolbarActivity() {

    private var messageClient: MessageClient? = null

    private val loadingFragment: LoadingFragment = LoadingFragment()
    private var setupFragment: SetupFragment? = null

    private val messageListener = MessageClient.OnMessageReceivedListener {
        if (it.path == REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH) {
            val hasNotiPolicyAccess = Boolean.fromByteArray(it.data)
            if (hasNotiPolicyAccess) {
                showAllSetFragment()
            } else {
                startSetup()
            }
        }
    }

    override fun getContentViewId(): Int = R.layout.activity_interrupt_filter_sync_helper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!androidVersionCompatible()) {
            showIncompatibleFragment()
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
        checkWatchNotiAccess()
    }

    private fun androidVersionCompatible(): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    private fun showLoading() {
        changeFragment(loadingFragment)
    }

    private fun showIncompatibleFragment() {
        setResult(RESULT_FAILED)
        changeFragment(IncompatibleFragment())
    }

    private fun showAllSetFragment() {
        setResult(RESULT_OK)
        changeFragment(AllSetFragment())
    }

    private fun startSetup() {
        if (setupFragment == null) setupFragment = SetupFragment()
        changeFragment(setupFragment!!)
    }

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .commit()
    }

    fun checkWatchNotiAccess() {
        showLoading()
        Wearable.getCapabilityClient(this)
                .getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE)
                .addOnCompleteListener {
                    if (it.isSuccessful && it.result != null && !it.result!!.nodes.isNullOrEmpty()) {
                        val node = it.result!!.nodes.first { node -> node.isNearby }
                        messageClient!!.sendMessage(node.id, REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH, null)
                    } else {
                        showIncompatibleFragment()
                    }
                }
    }

    companion object {
        const val RESULT_OK = 0
        const val RESULT_FAILED = 1
    }
}