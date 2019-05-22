/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.AtomicCounter
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
        when (it.path) {
            REQUEST_UNINSTALL_PACKAGE -> {
                if (it.data != null && it.data.isNotEmpty()) {
                    val packageName = String(it.data, Charsets.UTF_8)
                    val intent = Intent(Intent.ACTION_DELETE, "package:$packageName".toUri())
                    startActivity(intent)
//                    val intentSender = Compat.getForegroundService(this@AppManagerService, Intent(PACKAGE_UNINSTALL_RESULT)).intentSender
//                    val packageName = String(it.data, Charsets.UTF_8)
//                    packageManager.packageInstaller
//                            .uninstall(packageName, intentSender)
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
                            val packageInfo = AppPackageInfo(packageManager, packageManager.getPackageInfo(intent.data?.encodedSchemeSpecificPart, 0))
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
        return START_STICKY
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
                    "app_manager_service",
                    "App Manager Service Running",
                    NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
        val noti = NotificationCompat.Builder(this, "app_manager_service")
                .setContentTitle("App Manager Running")
                .setContentText("App Manager is currently running on your phone")
                .setSmallIcon(R.drawable.ic_sync)
                .setOngoing(true)
                .setShowWhen(false)
                .setUsesChronometer(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        startForeground(AtomicCounter.getInt(), noti)
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

    private fun sendAllAppsMessage() {
        val packagesToSend = ArrayList<AppPackageInfo>()
        packageManager.getInstalledPackages(0).forEach { packageInfo ->
            if ((packageInfo.applicationInfo?.flags?.and((ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))) == 0) {
                packagesToSend.add(AppPackageInfo(packageManager, packageInfo))
            }
        }
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
    }
}
