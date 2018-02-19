package com.boswelja.devicemanager.complications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.ConfirmationActivity
import android.util.Log
import com.boswelja.devicemanager.common.Config
import com.boswelja.devicemanager.common.Utils
import com.boswelja.devicemanager.ui.MainActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

class ActionReceiver: BroadcastReceiver(), GoogleApiClient.ConnectionCallbacks {

    private var actionType: Int? = Config.TYPE_EMPTY
    private lateinit var context: Context

    override fun onReceive(context: Context, intent: Intent?) {
        this.context = context
        actionType = intent?.getIntExtra("action", Config.TYPE_EMPTY)
    }


    override fun onConnected(bundle: Bundle?) {
        when (actionType) {
            Config.TYPE_LOCK_PHONE -> {
                val capabilityCallback = object: Utils.CapabilityCallbacks {
                    override fun noCapableDevices() {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                    }

                    override fun capableDeviceFound(node: Node?) {
                        lockDevice(node)
                    }
                }
                Utils.isCompanionAppInstalled(context, capabilityCallback)
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d("ActionReceiver", "Connection suspended")
    }

    private fun lockDevice(node: Node?) {
        val intent = Intent(context, ConfirmationActivity::class.java)
        //if (googleApiClient.isConnected && node != null) {
        //    Wearable.MessageApi.sendMessage(googleApiClient, node.id, Config.LOCK_PHONE_PATH, null)
        //            .setResultCallback {
        //                if (it.status.isSuccess) {
        //                    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
        //                            ConfirmationActivity.SUCCESS_ANIMATION)
        //                } else {
        //                    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
        //                            ConfirmationActivity.FAILURE_ANIMATION)
        //                }
        //            }
        //} else if (!googleApiClient.isConnecting && !googleApiClient.isConnected) {
        //    googleApiClient.connect()
        //    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
        //            ConfirmationActivity.FAILURE_ANIMATION)
        //} else {
        //    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
        //            ConfirmationActivity.FAILURE_ANIMATION)
        //}
        Wearable
                .getMessageClient(context)
                .sendMessage(
                    node!!.id,
                    Config.LOCK_PHONE_PATH,
                    null
                )
                .addOnSuccessListener({
                    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION)
                    context.startActivity(intent)
                })
                .addOnFailureListener({
                    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION)
                    context.startActivity(intent)
                })
    }
}