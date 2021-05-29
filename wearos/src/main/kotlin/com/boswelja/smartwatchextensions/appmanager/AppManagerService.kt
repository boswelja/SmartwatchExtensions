package com.boswelja.smartwatchextensions.appmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.appmanager.App
import com.boswelja.smartwatchextensions.common.appmanager.Messages.EXPECTED_APP_COUNT
import com.boswelja.smartwatchextensions.common.appmanager.Messages.PACKAGE_ADDED
import com.boswelja.smartwatchextensions.common.appmanager.Messages.PACKAGE_REMOVED
import com.boswelja.smartwatchextensions.common.appmanager.Messages.PACKAGE_UPDATED
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_OPEN_PACKAGE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.smartwatchextensions.common.appmanager.Messages.STOP_SERVICE
import com.boswelja.smartwatchextensions.common.toByteArray
import com.boswelja.smartwatchextensions.phoneStateStore
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class AppManagerService : LifecycleService() {

    private lateinit var messageClient: MessageClient

    private lateinit var phoneId: Flow<String>

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

        phoneId = phoneStateStore.data.map { it.id }

        messageClient = Wearable.getMessageClient(this)
        messageClient.addListener(messageReceiver)

        IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }.also { registerReceiver(packageChangeReceiver, it) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand received")

        if (intent?.action == ACTION_STOP_APP_MANAGER) {
            stopService()
        } else {
            startForeground(APP_MANAGER_NOTI_ID, createNotification())
            sendAllApps()
        }

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
        Timber.d("Stopping service")
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
        val stopIntent = Intent(this, AppManagerService::class.java).apply {
            action = ACTION_STOP_APP_MANAGER
        }.let {
            PendingIntent.getService(this, 333, it, PendingIntent.FLAG_IMMUTABLE)
        }
        return NotificationCompat.Builder(this, APP_MANAGER_NOTI_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_manager_noti_title))
            .setContentText(getString(R.string.app_manager_noti_desc))
            .setSmallIcon(R.drawable.ic_app_manager)
            .setOngoing(true)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.noti_ic_stop,
                getString(R.string.app_manager_stop),
                stopIntent
            )
            .build()
    }

    /**
     * Sends a message to the phone informing of a package removal.
     * @param packageName The name of the package that was removed.
     */
    private fun sendAppRemovedMessage(packageName: String?) {
        lifecycleScope.launch {
            phoneId.collect {
                val data = packageName?.toByteArray(Charsets.UTF_8)
                messageClient.sendMessage(it, PACKAGE_REMOVED, data)
            }
        }
    }

    /**
     * Sends a message to the phone informing of a package install.
     * @param packageInfo The [App] for the package that was installed.
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    private fun sendAppAddedMessage(packageInfo: App) {
        lifecycleScope.launch {
            val data = packageInfo.toByteArray()
            phoneId.collect {
                messageClient.sendMessage(it, PACKAGE_ADDED, data)
            }
        }
    }

    /**
     * Sends a message to the phone informing of a package update.
     * @param packageInfo The [App] for the package that was installed.
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    private fun sendAppUpdatedMessage(packageInfo: App) {
        lifecycleScope.launch {
            val data = packageInfo.toByteArray()
            phoneId.collect {
                messageClient.sendMessage(it, PACKAGE_UPDATED, data)
            }
        }
    }

    /**
     * Starts sending all apps to the connected phone.
     */
    private fun sendAllApps() {
        lifecycleScope.launch {
            val allPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
                .map {
                    App(packageManager, it)
                }
            phoneId.collect {
                // Send expected app count to the phone
                val data = allPackages.count().toByteArray()
                messageClient.sendMessage(it, EXPECTED_APP_COUNT, data)

                // Start sending apps
                allPackages.forEach { app ->
                    messageClient.sendMessage(it, PACKAGE_ADDED, app.toByteArray())
                }
            }
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
        private const val ACTION_STOP_APP_MANAGER = "stop-appmanager"
    }
}
