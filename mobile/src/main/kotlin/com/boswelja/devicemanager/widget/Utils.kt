package com.boswelja.devicemanager.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.widgetIdStore: DataStore<Preferences> by preferencesDataStore(name = "widget-ids")
val Context.widgetSettings: DataStore<Preferences> by preferencesDataStore(name = "widget-settings")
