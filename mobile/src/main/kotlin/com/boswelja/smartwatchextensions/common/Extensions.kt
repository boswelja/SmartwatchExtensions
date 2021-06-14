package com.boswelja.smartwatchextensions.common

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle

inline fun <reified T> Context.startActivity(
    flags: Int = 0,
    extras: Bundle? = null,
    options: ActivityOptions? = null
) {
    val intent = Intent(this, T::class.java).apply {
        extras?.let { putExtras(extras) }
        this.flags = flags
    }
    startActivity(intent, options?.toBundle())
}
