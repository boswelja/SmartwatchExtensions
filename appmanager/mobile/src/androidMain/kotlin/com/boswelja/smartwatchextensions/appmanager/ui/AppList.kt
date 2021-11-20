package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.appmanager.R
import com.boswelja.smartwatchextensions.appmanager.WatchApp
import com.boswelja.smartwatchextensions.appmanager.ui.AppListDefaults.DefaultIcon
import com.boswelja.smartwatchextensions.appmanager.ui.AppListDefaults.DefaultIconSize
import com.boswelja.smartwatchextensions.appmanager.ui.AppListDefaults.ItemContentPadding
import com.boswelja.smartwatchextensions.appmanager.ui.AppListDefaults.ListContentPadding

/**
 * A Composable for displaying a list of apps.
 * @param userApps The list of user apps.
 * @param disabledApps The list of disabled apps.
 * @param systemApps The list of system apps.
 * @param onAppClick Called when an app is clicked.
 * @param modifier [Modifier].
 * @param listPadding The padding values to apply to the list itself.
 * @param itemPadding The padding values to apply to items in the list.
 */
@Composable
fun AppList(
    userApps: List<WatchApp>,
    disabledApps: List<WatchApp>,
    systemApps: List<WatchApp>,
    onAppClick: (WatchApp) -> Unit,
    modifier: Modifier = Modifier,
    listPadding: PaddingValues = ListContentPadding,
    itemPadding: PaddingValues = ItemContentPadding
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = listPadding
    ) {
        appListItems(
            apps = userApps,
            onAppClick = onAppClick,
            header = {
                Text(stringResource(R.string.appmanager_section_user))
            },
            noApps = {

            },
            contentPadding = itemPadding
        )
        appListItems(
            apps = disabledApps,
            onAppClick = onAppClick,
            header = {
                Text(stringResource(R.string.appmanager_section_disabled))
            },
            noApps = {

            },
            contentPadding = itemPadding
        )
        appListItems(
            apps = systemApps,
            onAppClick = onAppClick,
            header = {
                Text(stringResource(R.string.appmanager_section_system))
            },
            noApps = {

            },
            contentPadding = itemPadding
        )
    }
}

/**
 * Adds all items associated with the given app list to the lazy list.
 * @param apps The list of apps to add.
 * @param onAppClick Called when an app is clicked.
 * @param header The header content for the apps. Note the padding is already applied.
 * @param noApps The content to display when [apps] is empty. Note the padding is already appled.
 * @param contentPadding The padding values to apply to items.
 */
fun LazyListScope.appListItems(
    apps: List<WatchApp>,
    onAppClick: (WatchApp) -> Unit,
    header: @Composable () -> Unit,
    noApps: @Composable () -> Unit,
    contentPadding: PaddingValues = ItemContentPadding
) {
    val itemModifier = Modifier.padding(contentPadding)
    item {
        Box(itemModifier) {
            header()
        }
    }
    if (apps.isNotEmpty()) {
        items(
            items = apps,
            key = { it.packageName }
        ) { app ->
            AppItem(
                app = app,
                onClick = onAppClick,
                modifier = itemModifier
            )
        }
    } else {
        item {
            Box(itemModifier) {
                noApps()
            }
        }
    }
}

/**
 * A Composable for displaying an app.
 * @param app The app to display.
 * @param onClick Called when the app is clicked.
 * @param modifier [Modifier].
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppItem(
    app: WatchApp,
    onClick: (WatchApp) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        text = { Text(app.label) },
        secondaryText = { Text(app.versionName) },
        icon = {
            Image(
                DefaultIcon,
                contentDescription = null,
                Modifier.size(DefaultIconSize)
            )
        },
        modifier = modifier.clickable { onClick(app) }
    )
}

/**
 * Contains default values used by [AppList].
 */
object AppListDefaults {
    private val ListContentHorizontalPadding = 16.dp
    private val ListContentVerticalPadding = 16.dp

    /**
     * The default padding values used by [AppList].
     */
    val ListContentPadding = PaddingValues(vertical = ListContentVerticalPadding)

    /**
     * The default padding values used by [AppItem].
     */
    val ItemContentPadding = PaddingValues(horizontal = ListContentHorizontalPadding)

    /**
     * The default app icon size for [AppInfo].
     */
    val DefaultIconSize = 40.dp

    /**
     * The default app icon to use for [AppInfo].
     */
    val DefaultIcon = Icons.Default.SyncProblem
}
