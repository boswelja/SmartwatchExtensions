package com.boswelja.devicemanager

import android.content.Context
import android.content.Intent
import androidx.wear.activity.ConfirmationActivity
import androidx.wear.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE
import androidx.wear.activity.ConfirmationActivity.EXTRA_MESSAGE
import androidx.wear.activity.ConfirmationActivity.FAILURE_ANIMATION
import androidx.wear.activity.ConfirmationActivity.SUCCESS_ANIMATION

object ConfirmationActivityHandler {

    private fun createIntent(context: Context, message: String?) =
        Intent(context, ConfirmationActivity::class.java)
            .putExtra(EXTRA_MESSAGE, message)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun failAnimation(context: Context, message: String? = null) {
        val intent =
            createIntent(context, message).putExtra(EXTRA_ANIMATION_TYPE, FAILURE_ANIMATION)
        context.startActivity(intent)
    }

    fun successAnimation(context: Context, message: String? = null) {
        val intent =
            createIntent(context, message).putExtra(EXTRA_ANIMATION_TYPE, SUCCESS_ANIMATION)
        context.startActivity(intent)
    }
}
