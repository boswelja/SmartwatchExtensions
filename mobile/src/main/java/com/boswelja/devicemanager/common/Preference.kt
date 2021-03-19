package com.boswelja.devicemanager.common

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.preference.PreferenceManager

@Composable
fun rememberSharedPreferences(): SharedPreferences {
    val context = LocalContext.current
    return remember {
        PreferenceManager.getDefaultSharedPreferences(context)
    }
}
