package com.boswelja.devicemanager.ui.base

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.boswelja.devicemanager.R

abstract class BaseDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, R.style.AppTheme_AlertDialog)
    }
}