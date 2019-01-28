package com.boswelja.devicemanager.preference

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import java.util.concurrent.TimeUnit

class SeekbarDialogPreference(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : DialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.dialogPreferenceStyle)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, defStyleAttr)

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
            sharedPreferences.getInt(PreferenceKey.BATTERY_SYNC_INTERVAL_KEY, 900000)
        }
        setSummary()
    }

    public fun setSummary() {
        summary = String.format(context.getString(R.string.battery_refresh_summary), TimeUnit.MILLISECONDS.toMinutes(value.toLong()))
    }
}