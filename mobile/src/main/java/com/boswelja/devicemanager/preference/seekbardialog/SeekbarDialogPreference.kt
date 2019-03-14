/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.preference.seekbardialog

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.boswelja.devicemanager.R
import java.util.concurrent.TimeUnit

class SeekbarDialogPreference(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : DialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    val minValue: Int
    val maxValue: Int
    val step: Int

    var value: Int = 0

    init {
        dialogLayoutResource = R.layout.dialog_seekbar_pref

        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.SeekbarDialogPref, defStyleAttr, defStyleRes)
        minValue = styledAttrs.getInt(R.styleable.SeekbarDialogPref_minValue, 0)
        maxValue = styledAttrs.getInt(R.styleable.SeekbarDialogPref_maxValue, 10)
        step = styledAttrs.getInt(R.styleable.SeekbarDialogPref_stepSize, 1)
        styledAttrs.recycle()
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a?.getInt(index, 0)!!
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = if (defaultValue != null) {
            defaultValue as Int
        } else {
            sharedPreferences.getInt(key, 0)
        }
        setSummary()
    }

    fun setSummary() {
        summary = String.format(context.getString(R.string.battery_sync_interval_summary), TimeUnit.MILLISECONDS.toMinutes(value.toLong()))
    }
}