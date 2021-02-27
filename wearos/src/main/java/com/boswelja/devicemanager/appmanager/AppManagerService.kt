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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Extensions.toByteArray
import com.boswelja.devicemanager.common.LifecycleAwareTimer
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.appmanager.Messages.EXPECTED_APP_COUNT
import com.boswelja.devicemanager.common.appmanager.Messages.PACKAGE_ADDED
import com.boswelja.devicemanager.common.appmanager.Messages.PACKAGE_REMOVED
import com.boswelja.devicemanager.common.appmanager.Messages.PACKAGE_UPDATED
import com.boswelja.devicemanager.common.appmanager.Messages.REQUEST_OPEN_PACKAGE
import com.boswelja.devicemanager.common.appmanager.Messages.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.devicemanager.common.appmanager.Messages.SERVICE_RUNNING
import com.boswelja.devicemanager.common.appmanager.Messages.STOP_SERVICE
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import timber.log.Timber

class AppManagerService : LifecycleService() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var messageClient: MessageClient
    private lateinit var serviceRunningNotifier: LifecycleAwareTimer

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
            Timber.d("onReceive($context, $intent) called")
            intent?.data?.let { data ->
                val isReplacingPackage = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                val packageName = data.encodedSchemeSpecificPart
                when (intent.action) {
                    Intent.ACTION_PACKAGE_ADDED -> {
                        App(
                            packageManager, packageManager.getPackageInfo(packageName, 0)
                        ).also {
                            if (isReplacingPackage) {
                                sendAppUpdatedMessage(it)
                            } else {
                                sendAppAddedMessage(it)
                            }
                        }
                    }
                    Intent.ACTION_PACKAGE_REMOVED -> {
                        if (!isReplacingPackage) {
                            sendAppRemovedMessage(packageName)
                        } else {
                            Timber.i(
                                "Package removed, but system indicated it's being replaced."
                            )
                        }
                    }
                    else -> Timber.w("Unknown intent received")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        Timber.d("onCreate() called")

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        messageClient = Wearable.getMessageClient(this)

        phoneId = sharedPreferences.getString(PHONE_ID_KEY, "")

        messageClient.addListener(messageReceiver)

        IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }.also { registerReceiver(packageChangeReceiver, it) }

        serviceRunningNotifier = LifecycleAwareTimer(10) {
            messageClient.sendMessage(phoneId!!, SERVICE_RUNNING, null)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand received")
        startForeground(APP_MANAGER_NOTI_ID, createNotification())
        sendAllApps()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Timber.d("onDestroy() called")
        unregisterReceiver(packageChangeReceiver)
        messageClient.removeListener(messageReceiver)
        super.onDestroy()
    }

    /** Stops the service. */
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
            val notificationManager = getSystemService<NotificationManager>()
            if (notificationManager?.getNotificationChannel(APP_MANAGER_NOTI_CHANNEL_ID) == null) {
                NotificationChannel(
                    APP_MANAGER_NOTI_CHANNEL_ID,
                    getString(R.string.app_manager_noti_channel_title),
                    NotificationManager.IMPORTANCE_LOW
                )
                    .also { notificationManager?.createNotificationChannel(it) }
            }
        }
        return NotificationCompat.Builder(this, APP_MANAGER_NOTI_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_manager_noti_title))
            .setContentText(getString(R.string.app_manager_noti_desc))
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
     * @param packageInfo The [App] for the package that was installed.
     */
    private fun sendAppAddedMessage(packageInfo: App) {
        val data = packageInfo.toByteArray()
        messageClient.sendMessage(phoneId!!, PACKAGE_ADDED, data)
    }

    /**
     * Sends a message to the phone informing of a package update.
     * @param packageInfo The [App] for the package that was installed.
     */
    private fun sendAppUpdatedMessage(packageInfo: App) {
        val data = packageInfo.toByteArray()
        messageClient.sendMessage(phoneId!!, PACKAGE_UPDATED, data)
    }

    /**
     * Starts sending all apps to the connected phone.
     */
    private fun sendAllApps() {
        val allPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS).map {
            App(packageManager, it)
        }
        // Send expected app count to the phone
        val data = allPackages.count().toByteArray()
        messageClient.sendMessage(phoneId!!, EXPECTED_APP_COUNT, data)

        // Start sending apps
        allPackages.forEach {
            messageClient.sendMessage(phoneId!!, PACKAGE_ADDED, it.toByteArray())
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
            }.also { startActivity(it) }
        } else {
            sendAppRemovedMessage(packageName)
        }
    }

    /**
     * Gets a launch intent for a given package and try start a new activity for it.
     * @param packageName The name of the package to try open.
     */
    private fun openPackage(packageName: String) {
        packageManager.getLaunchIntentForPackage(packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            ?.also { startActivity(it) }
    }

    companion object {
        private const val APP_MANAGER_NOTI_CHANNEL_ID = "app_manager_service"
        private const val APP_MANAGER_NOTI_ID = 906
    }
}
