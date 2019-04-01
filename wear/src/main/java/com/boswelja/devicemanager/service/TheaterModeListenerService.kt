package com.boswelja.devicemanager.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.AtomicCounter
import com.boswelja.devicemanager.common.Compat

class TheaterModeListenerService : Service() {

    private val dndSyncWithTheaterModeNotiCategoryKey = "dnd_sync_with_theater_mode"

    private val theaterModeObserver = TheaterModeObserver(Handler())

    override fun onBind(intent: Intent?): IBinder? {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return null
    }

    override fun onCreate() {
        super.onCreate()
        applicationContext.contentResolver.registerContentObserver(
                Settings.Global.CONTENT_URI, true,
                theaterModeObserver)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                    dndSyncWithTheaterModeNotiCategoryKey,
                    getString(R.string.dnd_sync_with_theater_mode_noti_title),
                    NotificationManager.IMPORTANCE_LOW).apply {
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }
            notiManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val noti = NotificationCompat.Builder(this, dndSyncWithTheaterModeNotiCategoryKey)
                .setContentTitle(getString(R.string.dnd_sync_with_theater_mode_noti_title))
                .setContentText(getString(R.string.dnd_sync_with_theater_mode_noti_content))
                .setSmallIcon(R.drawable.ic_sync)
                .setOngoing(true)
                .setShowWhen(false)
                .setUsesChronometer(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        startForeground(AtomicCounter.getInt(), noti)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        applicationContext.contentResolver.unregisterContentObserver(theaterModeObserver)
    }

    private inner class TheaterModeObserver(handler: Handler) : ContentObserver(handler) {

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val context = this@TheaterModeListenerService
            val isTheaterModeOn = Utils.isTheaterModeOn(context)
            Compat.setInterruptionFilter(context, isTheaterModeOn)
        }

    }
}