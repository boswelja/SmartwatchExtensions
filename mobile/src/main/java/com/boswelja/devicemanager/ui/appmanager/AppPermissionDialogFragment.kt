package com.boswelja.devicemanager.ui.appmanager

import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseDialogFragment

class AppPermissionDialogFragment(private val requestedPermissions: Array<String>) : BaseDialogFragment() {

    private lateinit var permissions: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        val processedPermissions = ArrayList<String>()
        for (permission in requestedPermissions) {
            try {
                val permissionInfo = context?.packageManager?.getPermissionInfo(permission, PackageManager.GET_META_DATA)
                processedPermissions.add(getString(permissionInfo?.labelRes!!).capitalize())
            } catch (ignored: Exception) {
                processedPermissions.add(permission)
            }
        }
        processedPermissions.sort()
        permissions = processedPermissions.toTypedArray()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setPositiveButton(R.string.dialog_button_close) { dialog, _ -> dialog.dismiss() }
                .setItems(permissions, null)
                .setTitle("Requested Permissions")
                .create()
    }
}