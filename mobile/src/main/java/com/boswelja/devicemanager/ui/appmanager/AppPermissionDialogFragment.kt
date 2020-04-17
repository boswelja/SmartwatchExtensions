/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseDialogFragment
import java.util.Locale
import kotlin.collections.ArrayList

@ExperimentalStdlibApi
class AppPermissionDialogFragment(private val requestedPermissions: Array<String>) : BaseDialogFragment() {

    private lateinit var permissions: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        processPermissions()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!)
                .setPositiveButton(R.string.dialog_button_close) { dialog, _ -> dialog.dismiss() }
                .setItems(permissions, null)
                .setTitle(R.string.app_info_requested_permissions_dialog_title)
                .create()
    }

    /**
     * Attempts to convert system permissions strings into something meaningful to the user.
     * Fallback is to just use the system strings.
     */
    private fun processPermissions() {
        val processedPermissions = ArrayList<String>()
        for (permission in requestedPermissions) {
            try {
                val permissionInfo = context?.packageManager?.getPermissionInfo(permission, PackageManager.GET_META_DATA)
                processedPermissions.add(getString(permissionInfo?.labelRes!!).capitalize(Locale.getDefault()))
            } catch (ignored: Exception) {
                processedPermissions.add(permission)
            }
        }
        processedPermissions.sort()
        permissions = processedPermissions.toTypedArray()
    }

    fun show(fragmentManager: FragmentManager) =
            show(fragmentManager, "RequestedPermissionsDialog")
}
