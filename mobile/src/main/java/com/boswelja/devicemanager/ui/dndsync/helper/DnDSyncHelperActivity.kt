/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.dndsync.helper

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Extensions.fromByteArray
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_SDK_INT_PATH
import com.boswelja.devicemanager.databinding.ActivityDndSyncHelperBinding
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.ui.common.LoadingFragment
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import java.math.BigInteger
import timber.log.Timber

class DnDSyncHelperActivity : BaseToolbarActivity() {

    private val watchConnectionManagerConnection = object : WatchManager.Connection() {
        override fun onWatchManagerBound(watchManager: WatchManager) {
            Timber.d("onWatchManagerBound() called")
            watchConnectionManager = watchManager
            checkWatchSystemVersion()
        }

        override fun onWatchManagerUnbound() {
            Timber.w("onWatchManagerUnbound called")
            watchConnectionManager = null
        }
    }

    private val messageListener = MessageClient.OnMessageReceivedListener {
        Timber.d("Message received")
        when (it.path) {
            REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH -> {
                val hasNotiPolicyAccess = Boolean.fromByteArray(it.data)
                Timber.i("Watch has notification policy access = $hasNotiPolicyAccess")
                if (hasNotiPolicyAccess) {
                    showAllSetFragment()
                } else {
                    showSetupFragment()
                }
            }
            REQUEST_SDK_INT_PATH -> {
                val sdkInt = BigInteger(it.data).toInt()
                Timber.i("Watch SDK = $sdkInt")
                if (sdkInt > Build.VERSION_CODES.O) {
                    showWatchVersionError()
                } else {
                    checkWatchNotiAccess(false)
                }
            }
        }
    }

    private lateinit var binding: ActivityDndSyncHelperBinding

    private var watchConnectionManager: WatchManager? = null
    private var messageClient: MessageClient? = null

    private val loadingFragment: LoadingFragment = LoadingFragment()
    private var errorFragment: ErrorFragment? = null
    private var setupFragment: SetupFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate() called")

        binding = ActivityDndSyncHelperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.toolbarLayout.toolbar, showUpButton = true)
        showLoadingFragment(false)

        setResult(RESULT_USER_DISMISSED)

        messageClient = Wearable.getMessageClient(this)
        messageClient!!.addListener(messageListener)

        WatchManager.bind(this, watchConnectionManagerConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy() called")
        messageClient?.removeListener(messageListener)
        unbindService(watchConnectionManagerConnection)
    }

    /**
     * Calls [changeFragment] with an instance of a [LoadingFragment].
     */
    private fun showLoadingFragment(animate: Boolean, reverse: Boolean = false) {
        Timber.i("Showing loading fragment")
        changeFragment(loadingFragment, animate = animate, reverse = reverse)
    }

    /**
     * If not already visible, shows an [ErrorFragment] and shows watch version incompatible error.
     */
    private fun showWatchVersionError() {
        Timber.d("showWatchVersionError() called")
        showErrorFragment(ErrorFragment.Error.WATCH_VERSION_INCOMPATIBLE)
    }

    /**
     * If not already visible, shows an [ErrorFragment] and shows watch unreachable error.
     */
    private fun showWatchNullError() {
        Timber.d("showWatchNullError() called")
        showErrorFragment(ErrorFragment.Error.WATCH_UNREACHABLE)
    }

    /**
     * Calls [changeFragment] with an instance of [ErrorFragment].
     */
    private fun showErrorFragment(error: ErrorFragment.Error) {
        Timber.i("Showing error fragment")
        setResult(RESULT_FAILED)
        if (errorFragment == null) errorFragment = ErrorFragment(error)
        changeFragment(errorFragment!!)
    }

    /**
     * Calls [changeFragment] with an instance of [AllSetFragment].
     */
    private fun showAllSetFragment() {
        Timber.i("Showing all set fragment")
        setResult(RESULT_OK)
        changeFragment(AllSetFragment())
    }

    /**
     * Calls [changeFragment] with an instance of [SetupFragment].
     * Reverses animation direction and sets an error message if needed.
     */
    private fun showSetupFragment() {
        Timber.i("Showing setup fragment")
        if (setupFragment == null) {
            setupFragment = SetupFragment()
            changeFragment(setupFragment!!)
        } else {
            Timber.i("Permissions not granted")
            setupFragment!!.setErrorMessage(getString(R.string.interrupt_filter_sync_helper_permission_not_granted))
            changeFragment(setupFragment!!, reverse = true)
        }
    }

    /**
     * Changes the visible fragment to a given instance.
     * @param fragment The [Fragment] to show.
     * @param animate true if the transition should be animated, false otherwise.
     * @param reverse true if the animation should be reversed, false otherwise.
     * Not used if animate is false.
     */
    private fun changeFragment(fragment: Fragment, animate: Boolean = true, reverse: Boolean = false) {
        Timber.d("changeFragment() called")
        supportFragmentManager.beginTransaction().apply {
            if (animate) {
                Timber.i("Animating transition")
                if (reverse) {
                    Timber.i("Animation reversed")
                    setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                } else {
                    setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }
            replace(R.id.fragment_holder, fragment)
        }.also {
            try {
                it.commitNow()
            } catch (e: IllegalStateException) {
                Timber.e(e)
            }
        }
    }

    /**
     * Request the connected watches SDK version, received in [messageListener].
     */
    private fun checkWatchSystemVersion() {
        Timber.d("checkWatchSystemVersion() called")
        val connectedWatchId = watchConnectionManager?.connectedWatch?.id
        if (!connectedWatchId.isNullOrEmpty()) {
            messageClient!!.sendMessage(connectedWatchId, REQUEST_SDK_INT_PATH, null)
        } else {
            showWatchNullError()
        }
    }

    /**
     * Requests the connected watches notification policy access status,
     * received in [messageListener].
     */
    fun checkWatchNotiAccess(animate: Boolean = true) {
        Timber.d("checkWatchNotiAccess() called")
        showLoadingFragment(animate = animate)
        val connectedWatchId = watchConnectionManager?.connectedWatch?.id
        if (!connectedWatchId.isNullOrEmpty()) {
            messageClient!!.sendMessage(connectedWatchId, REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH, null)
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
