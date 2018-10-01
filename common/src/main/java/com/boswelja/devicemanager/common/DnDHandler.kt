package com.boswelja.devicemanager.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class DnDHandler : Service() {

    private lateinit var prefs: SharedPreferences
    private lateinit var notificationManager: NotificationManager
    private var dndChangeReceiver: DnDChangeReceiver? = null
    private var dndSyncSend: Boolean = false
    private val prefChangeListener: PreferenceChangeListener = PreferenceChangeListener()
    private lateinit var uuid: String

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener)

        uuid = CommonUtils.getUID(prefs)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        updateDnDSyncSend()

        createNotificationChannel()

        val notiBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, References.DND_SYNC_NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setContentTitle("DnD Sync Active")
                .setContentText("Syncing Do Not Disturb status between your phone and watch")
                .setSmallIcon(R.drawable.ic_sync)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
        startForeground(155216, notiBuilder.build())

        updateDnD()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun updateDnDSyncSend() {
        val newValue = prefs.getBoolean(PreferenceKey.DND_SYNC_SEND_KEY, dndSyncSend)
        if (newValue != dndSyncSend) {
            Log.d("DnDHandler", "DnD Sync Send changed to" + newValue.toString())
            dndSyncSend = newValue
            dndSyncSendChanged()
        }
    }

    private fun dndSyncSendChanged() {
        if (dndSyncSend) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            if (dndChangeReceiver == null) dndChangeReceiver = DnDChangeReceiver()
            registerReceiver(dndChangeReceiver, intentFilter)
        } else if (dndChangeReceiver != null) {
            try {
                unregisterReceiver(dndChangeReceiver)
            } catch (ignored: IllegalArgumentException) {}
            stopForeground(true)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(References.DND_SYNC_NOTIFICATION_CHANNEL_ID)
            if (notificationManager.getNotificationChannel(References.DND_SYNC_NOTIFICATION_CHANNEL_ID) == null) {
                val notiChannel = NotificationChannel(References.DND_SYNC_NOTIFICATION_CHANNEL_ID, "DnD Sync", NotificationManager.IMPORTANCE_LOW)
                notiChannel.enableLights(false)
                notiChannel.enableVibration(false)
                notiChannel.setShowBadge(false)
                notificationManager.createNotificationChannel(notiChannel)
            }
        }
    }

    private fun updateDnD() {
        val currentInterruptFilter = notificationManager.currentInterruptionFilter
        val dndEnabled: Boolean =
                (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS) ||
                        (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) ||
                        (currentInterruptFilter == NotificationManager.INTERRUPTION_FILTER_NONE)
        val dataClient = Wearable.getDataClient(this)
        val putDataMapReq = PutDataMapRequest.create("/dndStatus")
        putDataMapReq.dataMap.putBoolean(References.NEW_DND_STATE_PATH, dndEnabled)
        putDataMapReq.dataMap.putString(References.NEW_DND_STATE_CHANGED_BY_PATH, CommonUtils.getUID(this))
        putDataMapReq.setUrgent()
        dataClient.putDataItem(putDataMapReq.asPutDataRequest())
        Log.d("DnDSyncService", "DnD Active: $dndEnabled")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dndSyncSend && dndChangeReceiver != null) {
            try {
                unregisterReceiver(dndChangeReceiver)
            } catch (ignored: IllegalArgumentException) {}
        }
        Log.d("DnDHandler", "Stopping service")
    }

    private inner class PreferenceChangeListener : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
            when (key) {
                PreferenceKey.DND_SYNC_SEND_KEY -> updateDnDSyncSend()
                PreferenceKey.DND_SYNC_ENABLED_KEY -> {
                    if (!prefs?.getBoolean(key, false)!!) {
                        stopForeground(true)
                    }
                }
            }
        }
    }

    private inner class DnDChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED) {
                Log.d("DnDHandler", "DnD Changed")
                updateDnD()
            }
        }
    }
}