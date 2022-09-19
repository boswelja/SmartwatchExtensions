package com.boswelja.smartwatchextensions.core.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * A [DataStore] for tracking widget IDs.
 */
val Context.widgetIdStore: DataStore<Preferences> by preferencesDataStore(name = "widget-ids")
