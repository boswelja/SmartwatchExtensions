/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.common.appmanager.AppPackageInfoList
import com.boswelja.devicemanager.common.appmanager.References.ERROR
import com.boswelja.devicemanager.common.appmanager.References.GET_ALL_PACKAGES
import com.boswelja.devicemanager.common.appmanager.References.PACKAGE_ADDED
import com.boswelja.devicemanager.common.appmanager.References.PACKAGE_REMOVED
import com.boswelja.devicemanager.common.appmanager.References.REQUEST_OPEN_PACKAGE
import com.boswelja.devicemanager.common.appmanager.References.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.devicemanager.common.appmanager.References.STOP_SERVICE
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class AppManagerService : Service() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var messageClient: MessageClient

    private var phoneId: String? = null

    private val messageReceiver = MessageClient.OnMessageReceivedListener {
        when (it.path) {
            REQUEST_UNINSTALL_PACKAGE -> {
                getPackageNameFromBytes(it.data)?.also { packageName ->
                    requestUninstallPackage(packageName)
                }
            }
            REQUEST_OPEN_PACKAGE -> {
                getPackageNameFromBytes(it.data)?.also { packageName ->
                    openPackage(packageName)
                }
            }
            STOP_SERVICE -> {
                stopService()
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
                            AppPackageInfo(
                                    packageManager,
                                    packageManager.getPackageInfo(intent.data!!.encodedSchemeSpecificPart, 0))
                                    .also {
                                        sendAppAddedMessage(it)
                                    }
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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        phoneId = sharedPreferences.getString(PHONE_ID_KEY, "")

        messageClient = Wearable.getMessageClient(this).also {
            it.addListener(messageReceiver)
        }

        IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }.also {
            registerReceiver(packageChangeReceiver, it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification().also {
            startForeground(APP_MANAGER_NOTI_ID, it)
        }
        sendAllAppsMessage()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(packageChangeReceiver)
        messageClient.removeListener(messageReceiver)
        super.onDestroy()
    }

    /**
     * Stops the service.
     */
    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }

    /**
     * Create the App Manager notification. Will also create the notification channel if needed.
     * @return The [Notification] for the App Manager.
     */
    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(APP_MANAGER_NOTI_CHANNEL_ID) == null) {
                NotificationChannel(
                        APP_MANAGER_NOTI_CHANNEL_ID,
                        getString(R.string.app_manager_service_noti_channel_title),
                        NotificationManager.IMPORTANCE_LOW).also {
                    notificationManager.createNotificationChannel(it)
                }
            }
        }
        return NotificationCompat.Builder(this, APP_MANAGER_NOTI_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_manager_service_noti_title))
                .setContentText(getString(R.string.app_manager_service_noti_desc))
                .setSmallIcon(R.drawable.ic_app_manager)
                .setOngoing(true)
                .setShowWhen(false)
                .setUsesChronometer(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
    }

    /**
     * Sends a message to the phone informing of a package removal.
     * @param packageName The name of the package that was removed.
     */
    private fun sendAppRemovedMessage(packageName: String?) {
        val data = packageName?.toByteArray(Charsets.UTF_8)
        messageClient.sendMessage(phoneId!!, PACKAGE_REMOVED, data)
    }

    /**
     * Sends a message to the phone informing of a package install.
     * @param packageInfo The [AppPackageInfo] for the package that was installed.
     */
    private fun sendAppAddedMessage(packageInfo: AppPackageInfo) {
        val data = packageInfo.toByteArray()
        messageClient.sendMessage(phoneId!!, PACKAGE_ADDED, data)
    }

    /**
     * Sends a message to the phone informing an error
     */
    private fun sendErrorMessage() {
        messageClient.sendMessage(phoneId!!, ERROR, null)
        stopService()
    }

    /**
     * Sends a message to the phone containing an [AppPackageInfoList] of all packages installed.
     */
    private fun sendAllAppsMessage() {
        try {
            val packagesToSend = AppPackageInfoList(packageManager)
            val data = packagesToSend.toByteArray()
            messageClient.sendMessage(phoneId!!, GET_ALL_PACKAGES, data)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            sendErrorMessage()
        }
    }

    /**
     * Converts a given [ByteArray] to a package name string.
     * @param data The [ByteArray] to convert.
     * @return THe package name, or null if data was invalid.
     */
    private fun getPackageNameFromBytes(data: ByteArray?): String? {
        return if (data != null && data.isNotEmpty()) {
            String(data, Charsets.UTF_8)
        } else {
            null
        }
    }

    /**
     * Checks whether a package is installed.
     * @param packageName The name of the package to check.
     * @return true if the package is installed, false otherwise.
     */
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (ignored: Exception) {
            false
        }
    }

    /**
     * If a package is installed, shows a prompt to allow the user to uninstall it.
     * @param packageName The name of the package to try uninstall.
     */
    private fun requestUninstallPackage(packageName: String) {
        if (isPackageInstalled(packageName)) {
            Intent().apply {
                action = Intent.ACTION_DELETE
                data = Uri.fromParts("package", packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }.also {
                startActivity(it)
            }
        } else {
            sendAppRemovedMessage(packageName)
        }
    }

    /**
     * Gets a launch intent for a given package and try start a new activity for it.
     * @param packageName The name of the package to try open.
     */
    private fun openPackage(packageName: String) {
        packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }?.also {
            startActivity(it)
        }
    }

    companion object {
        private const val APP_MANAGER_NOTI_CHANNEL_ID = "app_manager_service"
        private const val APP_MANAGER_NOTI_ID = 906
    }
}
