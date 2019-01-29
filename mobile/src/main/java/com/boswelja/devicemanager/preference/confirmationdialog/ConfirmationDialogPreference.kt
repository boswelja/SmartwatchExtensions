package com.boswelja.devicemanager.preference.confirmationdialog

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.preference.DialogPreference
import androidx.preference.PreferenceViewHolder
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey

class ConfirmationDialogPreference(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : DialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.dialogPreferenceStyle)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, defStyleAttr)

    private var value = false

    private lateinit var checkbox: AppCompatCheckBox

    init {
        widgetLayoutResource = R.layout.pref_widget_checkbox
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a?.getBoolean(index, false)!!
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = if (defaultValue != null) {
            defaultValue as Boolean
        } else {
            sharedPreferences.getBoolean(PreferenceKey.HIDE_APP_ICON_KEY, false)
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        checkbox = holder?.itemView?.findViewById(R.id.checkbox)!!
        checkbox.isChecked = value
    }

    override fun onClick() {
        if (!value) {
            super.onClick()
        } else {
            setValue(false)
            sharedPreferences.edit().putBoolean(key, value).apply()
            onPreferenceChangeListener?.onPreferenceChange(this, value)
        }
    }

    fun setValue(newValue: Boolean) {
        value = newValue
        checkbox.isChecked = value
    }

    fun getValue() : Boolean {
        return value
    }
}