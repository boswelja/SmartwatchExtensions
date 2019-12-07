/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseDialogFragment

class AppPermissionDialogFragment(private val requestedPermissions: Array<String>) : BaseDialogFragment() {

    private lateinit var permissions: Array<String>

    @SuppressLint("DefaultLocale")
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
                .setTitle(R.string.app_info_requested_permissions_dialog_title)
                .create()
    }
}
