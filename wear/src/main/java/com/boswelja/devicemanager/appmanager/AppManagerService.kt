/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.Utils.isAppInstalled
import com.boswelja.devicemanager.common.appmanager.References.ERROR
import com.boswelja.devicemanager.common.appmanager.References.GET_ALL_PACKAGES
import com.boswelja.devicemanager.common.appmanager.References.PACKAGE_ADDED
import com.boswelja.devicemanager.common.appmanager.References.PACKAGE_REMOVED
import com.boswelja.devicemanager.common.appmanager.References.REQUEST_OPEN_PACKAGE
import com.boswelja.devicemanager.common.appmanager.References.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.devicemanager.common.appmanager.References.STOP_SERVICE
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.common.appmanager.AppPackageInfoList
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class AppManagerService : Service() {

    private lateinit var messageClient: MessageClient

    private val messageReceiver = MessageClient.OnMessageReceivedListener {
        when (it.path) {
            REQUEST_UNINSTALL_PACKAGE -> {
                if (it.data != null && it.data.isNotEmpty()) {
                    val packageName = String(it.data, Charsets.UTF_8)
                    if (isAppInstalled(packageManager, packageName)) {
                        val intent = Intent(Intent.ACTION_DELETE, "package:$packageName".toUri())
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    } else {
                        sendAppRemovedMessage(packageName)
                    }
                }
            }
            REQUEST_OPEN_PACKAGE -> {
                if (it.data != null && it.data.isNotEmpty()) {
                    val packageName = String(it.data, Charsets.UTF_8)
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    if (intent != null) {
                        startActivity(intent)
                    }
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
            if (intent != null) {
                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    when (action) {
                        Intent.ACTION_PACKAGE_ADDED -> {
                            val packageInfo = AppPackageInfo(packageManager, packageManager.getPackageInfo(intent.data?.encodedSchemeSpecificPart!!, 0))
                            sendAppAddedMessage(packageInfo)
                        }
                        Intent.ACTION_PACKAGE_REMOVED -> {
                            sendAppRemovedMessage(intent.data?.encodedSchemeSpecificPart)
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        sendAllAppsMessage()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(packageChangeReceiver)
        messageClient.removeListener(messageReceiver)
        super.onDestroy()
    }

    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                    APP_MANAGER_NOTI_CHANNEL_ID,
                    getString(R.string.app_manager_service_noti_channel_title),
                    NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
        val noti = NotificationCompat.Builder(this, APP_MANAGER_NOTI_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_manager_service_noti_title))
                .setContentText(getString(R.string.app_manager_service_noti_desc))
                .setSmallIcon(R.drawable.ic_app_manager)
                .setOngoing(true)
                .setShowWhen(false)
                .setUsesChronometer(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        startForeground(APP_MANAGER_NOTI_ID, noti)
    }

    private fun sendAppRemovedMessage(packageName: String?) {
        Utils.getCompanionNode(this)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val node = it.result?.nodes?.firstOrNull()
                        if (node != null) {
                            val data = packageName?.toByteArray(Charsets.UTF_8)
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

    private fun sendErrorMessage() {
        Utils.getCompanionNode(this)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val node = it.result?.nodes?.firstOrNull()
                        if (node != null) {
                            messageClient.sendMessage(node.id!!, ERROR, null)
                        }
                    }
                    stopForeground(true)
                    stopSelf()
                }
    }

    private fun sendAllAppsMessage() {
        try {
            val packagesToSend = AppPackageInfoList(packageManager)
            Utils.getCompanionNode(this)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val node = it.result?.nodes?.firstOrNull()
                            if (node != null) {
                                val data = packagesToSend.toByteArray()
                                messageClient.sendMessage(node.id!!, GET_ALL_PACKAGES, data)
                            }
                        }
                    }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            sendErrorMessage()
        }
    }

    companion object {
        private const val APP_MANAGER_NOTI_CHANNEL_ID = "app_manager_service"
        private const val APP_MANAGER_NOTI_ID = 906
    }
}
