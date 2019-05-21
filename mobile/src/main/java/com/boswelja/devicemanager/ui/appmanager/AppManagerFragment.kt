/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

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
import com.boswelja.devicemanager.common.appmanager.AppManagerReferences
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

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
                (appsRecyclerView.adapter as AppsAdapter).remove(appPackageName)
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
            adapter = AppsAdapter()
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

    private fun setLoading(loading: Boolean) {
        if (loading) {
            appsLoadingSpinner.visibility = View.VISIBLE
            appsRecyclerView.visibility = View.INVISIBLE
        } else {
            appsLoadingSpinner.visibility = View.GONE
            appsRecyclerView.visibility = View.VISIBLE
        }
    }

    fun setShowSystemApps(show: Boolean) {
        (appsRecyclerView.adapter as AppsAdapter).setShowSystemApps(show)
    }
}
