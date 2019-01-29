package com.boswelja.devicemanager.preference.seekbardialog

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.preference.PreferenceDialogFragmentCompat
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import java.util.concurrent.TimeUnit

class SeekbarDialogPrefFragment : PreferenceDialogFragmentCompat() {

    private lateinit var key: String
    private lateinit var progressText: TextView

    override fun onDialogClosed(positiveResult: Boolean) {
        when (key) {
            PreferenceKey.BATTERY_SYNC_INTERVAL_KEY -> {
                val pref = preference as SeekbarDialogPreference
                if (positiveResult) {
                    pref.sharedPreferences.edit().putInt(key, pref.value).apply()
                    pref.setSummary()
                    pref.onPreferenceChangeListener?.onPreferenceChange(pref, pref.value)
                } else {
                    pref.value = pref.sharedPreferences.getInt(key, 900000)
                }
            }
        }
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        progressText = view?.findViewById(R.id.status)!!
        updateProgressText()
        val pref = preference as SeekbarDialogPreference
        val seekbar = view.findViewById<AppCompatSeekBar>(R.id.seekbar)!!
        seekbar.max = pref.maxValue - pref.minValue
        seekbar.progress = pref.value - pref.minValue

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                pref.value = progress + pref.minValue
                updateProgressText()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        val increase = view.findViewById<AppCompatImageButton>(R.id.increase)!!
        increase.setOnClickListener {
            seekbar.incrementProgressBy(pref.step)
        }
        val decrease = view.findViewById<AppCompatImageButton>(R.id.decrease)!!
        decrease.setOnClickListener {
            seekbar.incrementProgressBy(0 - pref.step)
        }
    }

    private fun updateProgressText() {
        progressText.text = String.format(context?.getString(R.string.battery_sync_interval_summary)!!, TimeUnit.MILLISECONDS.toMinutes((preference as SeekbarDialogPreference).value.toLong()))
    }

    companion object {
        fun newInstance(key: String) : SeekbarDialogPrefFragment {
            val frag = SeekbarDialogPrefFragment()
            val b = Bundle(1)
            b.putString(ARG_KEY, key)
            frag.arguments = b
            frag.key = key
            return frag
        }
    }
}