package com.boswelja.smartwatchextensions.common

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Start an activity with the given parameters.
 * @param flags The intent flags to launch the activity with, or 0 to use the default flags.
 * @param extras The intent extras to pass to the activity, or null if there is none.
 * @param options The activity options to launch the activity with, or null if there is none.
 */
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

/**
 * Start an activity with the given options.
 * @param options The activity options, or null if there is none.
 * @param intentBuilder A lambda that provides the base launch intent and allows applying changes.
 * The returned Intent will be used to start the activity.
 */
inline fun Context.startActivity(
    options: ActivityOptions? = null,
    intentBuilder: (Intent) -> Intent
) {
    val intent = Intent().let(intentBuilder)
    startActivity(intent, options?.toBundle())
}
