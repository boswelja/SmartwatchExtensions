package com.boswelja.devicemanager.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.os.IBinder
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.appmanager.AppManagerReferences
import com.boswelja.devicemanager.common.appmanager.AppManagerReferences.GET_ALL_PACKAGES
import com.boswelja.devicemanager.common.appmanager.AppManagerReferences.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class AppManagerService : Service() {

    private lateinit var messageClient: MessageClient

    private val messageReceiver = MessageClient.OnMessageReceivedListener {
        val messagePath = it.path
        when (messagePath) {
            GET_ALL_PACKAGES -> {
                val packages = packageManager.getInstalledPackages(0)

            }
            REQUEST_UNINSTALL_PACKAGE -> {
                if (it.data != null && it.data.isNotEmpty()) {
                    val intentSender = Compat.getForegroundService(this@AppManagerService,  Intent(PACKAGE_UNINSTALL_RESULT)).intentSender
                    val appPackageInfo = AppPackageInfo.fromByteArray(it.data)
                    packageManager.packageInstaller
                            .uninstall(appPackageInfo.packageName, intentSender)
                }
            }
        }
    }
    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (intent != null &&
                    (action == Intent.ACTION_PACKAGE_ADDED ||
                    action == Intent.ACTION_PACKAGE_REMOVED)) {
                val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                if (!isReplacing) {
                    val packageName = intent.dataString
                    val packageInfo = AppPackageInfo(packageManager.getPackageInfo(packageName, 0))
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
        super.onDestroy()
        unregisterReceiver(packageChangeReceiver)
        messageClient.removeListener(messageReceiver)
    }

    private fun sendAppRemovedMessage(packageInfo: AppPackageInfo) {

    }

    private fun sendAppAddedMessage(packageInfo: AppPackageInfo) {

    }

    companion object {
        val PACKAGE_UNINSTALL_RESULT = "package_uninstall_result"
    }
}