package com.boswelja.devicemanager.preference.confirmationdialog

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.preference.DialogPreference
import androidx.preference.PreferenceViewHolder
import com.boswelja.devicemanager.R

class ConfirmationDialogPreference(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : DialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.dialogPreferenceStyle)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, defStyleAttr)

    private var value = false

    private lateinit var checkbox: AppCompatCheckBox

    private val showOnEnable: Boolean
    private val showOnDisable: Boolean
    private val allowDisable: Boolean

    init {
        widgetLayoutResource = R.layout.pref_widget_checkbox
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.ConfirmationDialogPreference, defStyleAttr, defStyleRes)
        showOnEnable = styledAttrs.getBoolean(R.styleable.ConfirmationDialogPreference_showOnEnable, true)
        showOnDisable = styledAttrs.getBoolean(R.styleable.ConfirmationDialogPreference_showOnDisable, true)
        allowDisable = styledAttrs.getBoolean(R.styleable.ConfirmationDialogPreference_allowDisable, true)
        styledAttrs.recycle()
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a?.getBoolean(index, false)!!
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = if (defaultValue != null) {
            defaultValue as Boolean
        } else {
            sharedPreferences.getBoolean(key, false)
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        checkbox = holder?.itemView?.findViewById(R.id.checkbox)!!
        checkbox.isChecked = value
    }

    override fun onClick() {
        if ((value && allowDisable) || !value) {
            if ((!value && showOnEnable) || (value && showOnDisable)) {
                super.onClick()
            } else {
                setValue(!value)
            }
        }
    }

    fun setValue(newValue: Boolean) {
        if (value != newValue) {
            value = newValue
            try {
                checkbox.isChecked = value
            } catch (ignored: UninitializedPropertyAccessException) {}
            sharedPreferences.edit().putBoolean(key, value).apply()
            onPreferenceChangeListener?.onPreferenceChange(this, value)
        }
    }

    fun getValue() : Boolean {
        return value
    }
}