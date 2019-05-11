/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.preference.confirmationdialog

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.CompoundButton
import androidx.preference.DialogPreference
import androidx.preference.PreferenceViewHolder
import com.boswelja.devicemanager.R

class ConfirmationDialogPreference(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : DialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.dialogPreferenceStyle)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, defStyleAttr)

    private var value = false

    private lateinit var compoundButton: CompoundButton

    private val showOnEnable: Boolean
    private val showOnDisable: Boolean
    private val allowDisable: Boolean

    init {
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.ConfirmationDialogPreference, defStyleAttr, defStyleRes)
        showOnEnable = styledAttrs.getBoolean(R.styleable.ConfirmationDialogPreference_showOnEnable, true)
        showOnDisable = styledAttrs.getBoolean(R.styleable.ConfirmationDialogPreference_showOnDisable, true)
        allowDisable = styledAttrs.getBoolean(R.styleable.ConfirmationDialogPreference_allowDisable, true)
        styledAttrs.recycle()
        widgetLayoutResource = R.layout.pref_widget_checkbox
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any = a?.getBoolean(index, false)!!

    override fun onSetInitialValue(defaultValue: Any?) {
        value = if (defaultValue != null) {
            defaultValue as Boolean
        } else {
            sharedPreferences.getBoolean(key, false)
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        compoundButton = holder?.itemView?.findViewById(R.id.widget)!!
        setButtonChecked(value)
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
            if ((onPreferenceChangeListener == null) || (onPreferenceChangeListener?.onPreferenceChange(this, newValue) == true)) {
                value = newValue
                try {
                    setButtonChecked(value)
                } catch (ignored: UninitializedPropertyAccessException) {}
                sharedPreferences.edit().putBoolean(key, value).apply()
            }
        }
    }

    private fun setButtonChecked(checked: Boolean) {
        compoundButton.isChecked = checked
        if (checked) {
            compoundButton.contentDescription = context.getString(R.string.content_description_enabled)
        } else {
            compoundButton.contentDescription = context.getString(R.string.content_description_disabled)
        }
    }

    fun getValue(): Boolean {
        return value
    }
}
