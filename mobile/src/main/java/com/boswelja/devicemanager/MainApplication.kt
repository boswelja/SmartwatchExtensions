/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Process
import com.boswelja.devicemanager.bootorupdate.BootOrUpdateHandlerService
import com.boswelja.devicemanager.bootorupdate.updater.Updater
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.crashhandler.CrashHandlerActivity
import com.boswelja.devicemanager.crashhandler.CrashHandlerActivity.Companion.EXTRA_STACKTRACE
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ref.WeakReference
import kotlin.system.exitProcess
import timber.log.Timber

class MainApplication : Application() {

    private var defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
    private var isInForeground = true
    private var lastActivityCreated: WeakReference<Activity>? = null

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        setupCrashhandler()
        ensureUpdated()
    }

    /**
     * Checks the environment is up to date, otherwise starts a [BootOrUpdateHandlerService].
     */
    private fun ensureUpdated() {
        val updater = Updater(this)
        if (updater.needsUpdate) {
            Timber.i("Starting updater service")
            Intent(this, BootOrUpdateHandlerService::class.java).apply {
                action = Intent.ACTION_MY_PACKAGE_REPLACED
            }.also {
                Compat.startForegroundService(this, it)
            }
        }
    }

    /**
     * Set up our [CrashHandlerActivity].
     */
    private fun setupCrashhandler() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var startedActivities = 0

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity !is CrashHandlerActivity) {
                    lastActivityCreated = WeakReference(activity)
                }
            }

            override fun onActivityStarted(activity: Activity) {
                startedActivities++
                updateIsInBackground()
            }

            override fun onActivityResumed(activity: Activity) {
                // Do nothing
            }
            override fun onActivityPaused(activity: Activity) {
                // Do nothing
            }

            override fun onActivityStopped(activity: Activity) {
                startedActivities--
                updateIsInBackground()
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                // Do nothing
            }

            override fun onActivityDestroyed(activity: Activity) {
                // Do nothing
            }

            private fun updateIsInBackground() {
                isInForeground = startedActivities > 0
            }
        })

        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Timber.i("Uncaught exception occurred")
            if (isInForeground) {
                var stacktrace = ""
                StringWriter().use { sw ->
                    PrintWriter(sw).use { pw ->
                        exception.printStackTrace(pw)
                    }
                    stacktrace = sw.toString()
                }
                if (stacktrace.length > 131071) {
                    val truncatedText = "..."
                    stacktrace = stacktrace.substring(0, 131071 - truncatedText.length) + truncatedText
                }

                Intent(this, CrashHandlerActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(EXTRA_STACKTRACE, stacktrace)
                }.also {
                    Timber.i("Starting CrashHandlerActivity")
                    startActivity(it)
                }
            } else {
                defaultUncaughtExceptionHandler!!.uncaughtException(thread, exception)
            }

            lastActivityCreated?.get()?.finish()
            lastActivityCreated?.clear()

            killCurrentProcess()
        }
    }

    companion object {
        fun killCurrentProcess() {
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }
}
