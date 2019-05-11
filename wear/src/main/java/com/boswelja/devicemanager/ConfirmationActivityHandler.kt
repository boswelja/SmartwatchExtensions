/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.content.Context
import android.content.Intent
import androidx.wear.activity.ConfirmationActivity
import androidx.wear.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE
import androidx.wear.activity.ConfirmationActivity.EXTRA_MESSAGE
import androidx.wear.activity.ConfirmationActivity.FAILURE_ANIMATION
import androidx.wear.activity.ConfirmationActivity.OPEN_ON_PHONE_ANIMATION
import androidx.wear.activity.ConfirmationActivity.SUCCESS_ANIMATION

object ConfirmationActivityHandler {

    private fun createIntent(context: Context, message: String?) =
            Intent(context, ConfirmationActivity::class.java)
                    .putExtra(EXTRA_MESSAGE, message)

    fun openOnPhoneAnimation(context: Context, message: String? = null) {
        val intent = createIntent(context, message)
                .putExtra(EXTRA_ANIMATION_TYPE, OPEN_ON_PHONE_ANIMATION)
        context.startActivity(intent)
    }

    fun failAnimation(context: Context, message: String? = null) {
        val intent = createIntent(context, message)
                .putExtra(EXTRA_ANIMATION_TYPE, FAILURE_ANIMATION)
        context.startActivity(intent)
    }

    fun successAnimation(context: Context, message: String? = null) {
        val intent = createIntent(context, message)
                .putExtra(EXTRA_ANIMATION_TYPE, SUCCESS_ANIMATION)
        context.startActivity(intent)
    }
}
