package com.boswelja.devicemanager.ui.version

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseDialogFragment

class ChangelogDialogFragment : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setPositiveButton(R.string.dialog_button_close) { dialog, _ -> dialog.dismiss() }
                .setItems(R.array.version_changelog, null)
                .setTitle(R.string.dialog_changelog_title)
                .create()
    }
}