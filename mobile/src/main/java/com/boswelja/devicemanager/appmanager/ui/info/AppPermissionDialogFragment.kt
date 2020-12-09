/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui.info

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.recyclerview.adapter.StringAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale
import timber.log.Timber

class AppPermissionDialogFragment(private val requestedPermissions: Array<String>) :
    BottomSheetDialogFragment() {

    private val permissions: Array<String> by lazy { processPermissions() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated() called")
        view.findViewById<AppCompatTextView>(R.id.title)
            .setText(R.string.app_info_requested_permissions_dialog_title)
        view.findViewById<RecyclerView>(R.id.recyclerview).adapter = StringAdapter(permissions)
    }

    /**
     * Attempts to convert system permissions strings into something meaningful to the user.
     * Fallback is to just use the system strings.
     */
    private fun processPermissions(): Array<String> {
        val processedPermissions = ArrayList<String>()
        for (permission in requestedPermissions) {
            try {
                val permissionInfo =
                    context?.packageManager?.getPermissionInfo(
                        permission, PackageManager.GET_META_DATA
                    )
                processedPermissions.add(
                    getString(permissionInfo?.labelRes!!).capitalize(Locale.getDefault())
                )
            } catch (ignored: Exception) {
                processedPermissions.add(permission)
            }
        }
        processedPermissions.sort()
        return processedPermissions.toTypedArray()
    }

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, "RequestedPermissionsDialog")
}
