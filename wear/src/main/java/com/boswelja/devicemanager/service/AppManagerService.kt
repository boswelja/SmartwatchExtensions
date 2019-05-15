/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.Extensions.toByteArray
import com.boswelja.devicemanager.common.appmanager.AppManagerReferences.GET_ALL_PACKAGES
import com.boswelja.devicemanager.common.appmanager.AppManagerReferences.PACKAGE_ADDED
import com.boswelja.devicemanager.common.appmanager.AppManagerReferences.PACKAGE_REMOVED
import com.boswelja.devicemanager.common.appmanager.AppManagerReferences.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.devicemanager.common.appmanager.AppManagerReferences.STOP_SERVICE
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class AppManagerService : Service() {

    private lateinit var messageClient: MessageClient

    private val messageReceiver = MessageClient.OnMessageReceivedListener {
        val messagePath = it.path
        when (messagePath) {
            GET_ALL_PACKAGES -> {
                val packagesToSend = ArrayList<AppPackageInfo>()
                packageManager.getInstalledPackages(0).forEach { packageInfo ->
                    val appPackageInfo = AppPackageInfo(packageInfo)
                    packagesToSend.add(appPackageInfo)
                }
                sendAllAppsMessage(packagesToSend)
            }
            REQUEST_UNINSTALL_PACKAGE -> {
                if (it.data != null && it.data.isNotEmpty()) {
                    val intentSender = Compat.getForegroundService(this@AppManagerService, Intent(PACKAGE_UNINSTALL_RESULT)).intentSender
                    val appPackageInfo = AppPackageInfo.fromByteArray(it.data)
                    packageManager.packageInstaller
                            .uninstall(appPackageInfo.packageName, intentSender)
                }
            }
            STOP_SERVICE -> {
                stopForeground(true)
                stopSelf()
            }
        }
    }
    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (intent != null &&
                    (action == Intent.ACTION_PACKAGE_ADDED ||
                    action == Intent.ACTION_PACKAGE_REMOVED)) {
                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    val packageInfo = AppPackageInfo(packageManager.getPackageInfo(intent.dataString, 0))
                    when (action) {
                        Intent.ACTION_PACKAGE_ADDED -> {
                            sendAppAddedMessage(packageInfo)
                        }
                        Intent.ACTION_PACKAGE_REMOVED -> {
                            sendAppRemovedMessage(packageInfo)
                        }
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        messageClient = Wearable.getMessageClient(this)
        messageClient.addListener(messageReceiver)

        val packageEventIntentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }
        registerReceiver(packageChangeReceiver, packageEventIntentFilter)
    }

    override fun onDestroy() {
        unregisterReceiver(packageChangeReceiver)
        messageClient.removeListener(messageReceiver)
        super.onDestroy()
    }

    private fun sendAppRemovedMessage(packageInfo: AppPackageInfo) {
        Utils.getCompanionNode(this)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val node = it.result?.nodes?.firstOrNull()
                        if (node != null) {
                            val data = packageInfo.toByteArray()
                            messageClient.sendMessage(node.id!!, PACKAGE_REMOVED, data)
                        }
                    }
                }
    }

    private fun sendAppAddedMessage(packageInfo: AppPackageInfo) {
        Utils.getCompanionNode(this)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val node = it.result?.nodes?.firstOrNull()
                        if (node != null) {
                            val data = packageInfo.toByteArray()
                            messageClient.sendMessage(node.id!!, PACKAGE_ADDED, data)
                        }
                    }
                }
    }

    private fun sendAllAppsMessage(packages: ArrayList<AppPackageInfo>) {
        Utils.getCompanionNode(this)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val node = it.result?.nodes?.firstOrNull()
                        if (node != null) {
                            val data = packages.toByteArray()
                            messageClient.sendMessage(node.id!!, GET_ALL_PACKAGES, data)
                        }
                    }
                }
    }

    companion object {
        const val PACKAGE_UNINSTALL_RESULT = "package_uninstall_result"
    }
}
