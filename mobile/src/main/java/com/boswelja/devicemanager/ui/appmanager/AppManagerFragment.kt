/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Extensions.addFromByteArray
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.appmanager.AppManagerReferences
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.google.android.material.snackbar.Snackbar

class AppManagerFragment : Fragment() {

    private lateinit var messageClient: MessageClient

    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var appsLoadingSpinner: ProgressBar

    private val messageListener = MessageClient.OnMessageReceivedListener {
        when (it.path) {
            AppManagerReferences.PACKAGE_ADDED -> {
                val appPackageInfo = AppPackageInfo.fromByteArray(it.data)
                (appsRecyclerView.adapter as AppsAdapter).add(appPackageInfo)
            }
            AppManagerReferences.PACKAGE_REMOVED -> {
                val appPackageName = String(it.data, Charsets.UTF_8)
                val adapter = (appsRecyclerView.adapter as AppsAdapter)
                Snackbar.make(view!!, "Uninstalled ${adapter.getFromPackageName(appPackageName)}", Snackbar.LENGTH_LONG)
                adapter.remove(appPackageName)
            }
            AppManagerReferences.GET_ALL_PACKAGES -> {
                val allApps = ArrayList<AppPackageInfo>()
                allApps.addFromByteArray(it.data)
                (appsRecyclerView.adapter as AppsAdapter).setAllApps(allApps)
                setLoading(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        messageClient = Wearable.getMessageClient(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_app_manager, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appsLoadingSpinner = view.findViewById(R.id.apps_loading_spinner)
        appsRecyclerView = view.findViewById<RecyclerView>(R.id.apps_recyclerview).apply {
            layoutManager = LinearLayoutManager(
                    context!!,
                    LinearLayoutManager.VERTICAL,
                    false)
            adapter = AppsAdapter(this@AppManagerFragment)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    (activity as AppManagerActivity).elevateToolbar(recyclerView.canScrollVertically(-1))
                }
            })
        }

        setLoading(true)
    }

    override fun onResume() {
        super.onResume()
        messageClient.addListener(messageListener)
    }

    override fun onPause() {
        super.onPause()
        messageClient.removeListener(messageListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            APP_INFO_ACTIVITY_REQUEST_CODE -> {
                when (resultCode) {
                    AppInfoActivity.RESULT_REQUEST_UNINSTALL -> {
                        val app = data?.extras?.getSerializable(AppInfoActivity.EXTRA_APP_INFO) as AppPackageInfo
                        sendUninstallRequestMessage(app)
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            appsLoadingSpinner.visibility = View.VISIBLE
            appsRecyclerView.visibility = View.INVISIBLE
        } else {
            appsLoadingSpinner.visibility = View.GONE
            appsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun sendUninstallRequestMessage(appPackageInfo: AppPackageInfo) {
        Wearable.getCapabilityClient(context!!)
                .getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val node = it.result?.nodes?.firstOrNull { node -> node.isNearby }
                        if (node != null) {
                            messageClient.sendMessage(node.id, AppManagerReferences.REQUEST_UNINSTALL_PACKAGE, appPackageInfo.packageName.toByteArray(Charsets.UTF_8))
                        }
                    }
                }
    }

    fun onItemClick(appPackageInfo: AppPackageInfo) {
        (activity as AppManagerActivity).startAppManagerService()
        val intent = Intent(context, AppInfoActivity::class.java)
        intent.putExtra(AppInfoActivity.EXTRA_APP_INFO, appPackageInfo)
        startActivityForResult(intent, APP_INFO_ACTIVITY_REQUEST_CODE)
    }

    companion object {
        private const val APP_INFO_ACTIVITY_REQUEST_CODE = 22668
    }
}
