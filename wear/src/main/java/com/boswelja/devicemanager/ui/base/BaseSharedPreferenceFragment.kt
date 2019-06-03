package com.boswelja.devicemanager.ui.base

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager

abstract class BaseSharedPreferenceFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        super.onCreate(savedInstanceState)
    }
}