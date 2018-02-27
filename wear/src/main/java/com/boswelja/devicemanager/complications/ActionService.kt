package com.boswelja.devicemanager.complications

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.wearable.activity.ConfirmationActivity
import com.boswelja.devicemanager.common.Config
import com.boswelja.devicemanager.common.Utils
import com.boswelja.devicemanager.ui.MainActivity
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

class ActionService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private var action: String? = null;

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        action = intent!!.getStringExtra(Config.INTENT_ACTION_EXTRA)
        val capabilityCallback = object: Utils.CapabilityCallbacks {
            override fun noCapableDevices() {
                val activityIntent = Intent(this@ActionService, MainActivity::class.java)
                startActivity(activityIntent)
            }

            override fun capableDeviceFound(node: Node?) {
                lockDevice(node)
            }
        }
        Utils.isCompanionAppInstalled(this, capabilityCallback)
        return START_NOT_STICKY
    }

    private fun onFailed() {
        val intent = Intent(this, ConfirmationActivity::class.java)
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION)
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Failed to lock your phone")
        startActivity(intent)
    }

    private fun onSuccess() {
        val intent = Intent(this, ConfirmationActivity::class.java)
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION)
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Locked your phone")
        startActivity(intent)
    }

    private fun lockDevice(node: Node?) {
        Wearable.getMessageClient(this)
                .sendMessage(
                    node!!.id,
                    action!!,
                    null
                )
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener {
                    onFailed()
                }
                .addOnCompleteListener {
                    stopSelf()
                }
    }
}