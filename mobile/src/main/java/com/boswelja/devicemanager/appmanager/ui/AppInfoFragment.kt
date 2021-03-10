package com.boswelja.devicemanager.appmanager.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.App
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * A fragment for showing detailed info about a single [App].
 */
class AppInfoFragment : Fragment() {

    private val viewModel: AppManagerViewModel by activityViewModels()
    private val args: AppInfoFragmentArgs by navArgs()

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val modalSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
                val scaffoldState = rememberScaffoldState()
                val app = args.app
                ModalBottomSheetLayout(
                    sheetState = modalSheetState,
                    sheetContent = {
                        PermissionsSheet(permissions = processPermissions(app.requestedPermissions))
                    }
                ) {
                    Scaffold(scaffoldState = scaffoldState) {
                        Column(Modifier.fillMaxSize()) {
                            AppHeaderView(app = app)
                            ActionButtons(app = app, scaffoldState = scaffoldState)
                            PermissionsInfo(
                                permissions = app.requestedPermissions,
                                state = modalSheetState
                            )
                            AppInstallInfo(app = app)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PermissionsSheet(permissions: Array<String>) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                stringResource(R.string.app_info_requested_permissions_dialog_title),
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn {
                items(permissions) { permission ->
                    Text(
                        permission,
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
    }

    @Composable
    fun AppHeaderView(app: App) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val icon = app.icon?.bitmap?.asImageBitmap()
                ?: ImageBitmap.imageResource(R.drawable.android_head)
            Image(
                icon,
                null,
                Modifier.size(72.dp)
            )
            Text(
                app.label,
                style = MaterialTheme.typography.h6
            )
        }
    }

    @Composable
    fun ActionButtons(app: App, scaffoldState: ScaffoldState) {
        val scope = rememberCoroutineScope()
        val continueOnWatchText = stringResource(R.string.watch_manager_action_continue_on_watch)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    viewModel.sendOpenRequest(app)
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(continueOnWatchText, null)
                    }
                },
                Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.OpenInNew, null)
                Text(stringResource(R.string.app_info_open_button))
            }
            OutlinedButton(
                onClick = {
                    viewModel.sendUninstallRequest(app)
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(continueOnWatchText, null)
                    }
                },
                Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.Delete, null)
                Text(stringResource(R.string.app_info_uninstall_button))
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun PermissionsInfo(permissions: Array<String>, state: ModalBottomSheetState) {
        val scope = rememberCoroutineScope()
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable {
                    scope.launch {
                        state.show()
                    }
                }
        ) {
            Text(
                stringResource(R.string.app_info_requested_permissions_title),
                style = MaterialTheme.typography.body1
            )
            val permissionText = if (permissions.isNotEmpty()) {
                val resources = LocalContext.current.resources
                resources.getQuantityString(
                    R.plurals.app_info_requested_permissions_count,
                    permissions.count(),
                    permissions.count()
                )
            } else {
                stringResource(R.string.app_info_requested_permissions_none)
            }
            Text(
                permissionText,
                style = MaterialTheme.typography.body2
            )
        }
    }

    @Composable
    fun AppInstallInfo(app: App) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(
                    R.string.app_info_first_installed_prefix,
                    viewModel.formatDate(app.installTime)
                ),
                style = MaterialTheme.typography.body2
            )
            if (app.installTime < app.lastUpdateTime) {
                Text(
                    stringResource(
                        R.string.app_info_last_updated_prefix,
                        viewModel.formatDate(app.lastUpdateTime)
                    ),
                    style = MaterialTheme.typography.body2
                )
            }
            Text(
                stringResource(R.string.app_info_version_prefix, app.version),
                style = MaterialTheme.typography.body2
            )
        }
    }

    /**
     * Attempts to convert system permissions strings into something meaningful to the user.
     * Fallback is to just use the system strings.
     */
    private fun processPermissions(requestedPermissions: Array<String>): Array<String> {
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
}
