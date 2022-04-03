package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.appmanager.R
import com.boswelja.smartwatchextensions.appmanager.WatchAppWithIcon
import com.boswelja.smartwatchextensions.appmanager.ui.AppListDefaults.DefaultIcon
import com.boswelja.smartwatchextensions.appmanager.ui.AppListDefaults.DefaultIconSize
import com.boswelja.smartwatchextensions.appmanager.ui.AppListDefaults.DividerContentPadding
import com.boswelja.smartwatchextensions.appmanager.ui.AppListDefaults.ItemContentPadding
import com.boswelja.smartwatchextensions.appmanager.ui.AppListDefaults.ItemTextPadding
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
    userApps: List<WatchAppWithIcon>,
    disabledApps: List<WatchAppWithIcon>,
    systemApps: List<WatchAppWithIcon>,
    onAppClick: (WatchAppWithIcon) -> Unit,
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
                Text(stringResource(R.string.appmanager_section_user_empty))
            },
            contentPadding = itemPadding
        )
        dividerItem()
        appListItems(
            apps = disabledApps,
            onAppClick = onAppClick,
            header = {
                Text(stringResource(R.string.appmanager_section_disabled))
            },
            noApps = {
                Text(stringResource(R.string.appmanager_section_disabled_empty))
            },
            contentPadding = itemPadding
        )
        dividerItem()
        appListItems(
            apps = systemApps,
            onAppClick = onAppClick,
            header = {
                Text(stringResource(R.string.appmanager_section_system))
            },
            noApps = {
                Text(stringResource(R.string.appmanager_section_system_empty))
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
    apps: List<WatchAppWithIcon>,
    onAppClick: (WatchAppWithIcon) -> Unit,
    header: @Composable () -> Unit,
    noApps: @Composable () -> Unit,
    contentPadding: PaddingValues = ItemContentPadding
) {
    val itemModifier = Modifier.padding(contentPadding)
    item {
        Box(itemModifier) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                header()
            }
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
 * Adds a divider to the lazy list.
 * @param contentPadding The default padding to apply to the divider.
 */
fun LazyListScope.dividerItem(
    contentPadding: PaddingValues = DividerContentPadding
) {
    item {
        Divider(modifier = Modifier.padding(contentPadding))
    }
}

/**
 * A Composable for displaying an app.
 * @param app The app to display.
 * @param onClick Called when the app is clicked.
 * @param modifier [Modifier].
 */
@Composable
fun AppItem(
    app: WatchAppWithIcon,
    onClick: (WatchAppWithIcon) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .clickable(
                onClickLabel = app.label
            ) { onClick(app) }
            .then(modifier)
    ) {
        val iconModifier = Modifier.size(DefaultIconSize)
        if (app.icon == null) {
            Icon(
                DefaultIcon,
                contentDescription = null,
                modifier = iconModifier
            )
        } else {
            Image(
                bitmap = app.icon.asImageBitmap(),
                contentDescription = null,
                modifier = iconModifier
            )
        }
        Column(
            modifier = Modifier
                .padding(ItemTextPadding)
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = app.versionName,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Contains default values used by [AppList].
 */
object AppListDefaults {
    private val ListContentHorizontalPadding = 0.dp
    private val ListContentVerticalPadding = 0.dp

    private val ListItemHorizontalPadding = 16.dp
    private val ListItemVerticalPadding = 16.dp

    private val DividerVerticalPadding = 8.dp

    /**
     * The default padding values used by [AppList].
     */
    val ListContentPadding = PaddingValues(
        horizontal = ListContentHorizontalPadding,
        vertical = ListContentVerticalPadding
    )

    /**
     * The default padding values used by [AppItem].
     */
    val ItemContentPadding = PaddingValues(
        horizontal = ListItemHorizontalPadding,
        vertical = ListItemVerticalPadding
    )

    /**
     * The default padding values for [AppItem] text.
     */
    val ItemTextPadding = PaddingValues(start = ListItemHorizontalPadding)

    /**
     * The default padding values used by Divider.
     */
    val DividerContentPadding = PaddingValues(vertical = DividerVerticalPadding)

    /**
     * The default app icon size for [AppInfo].
     */
    val DefaultIconSize = 40.dp

    /**
     * The default app icon to use for [AppInfo].
     */
    val DefaultIcon = Icons.Default.SyncProblem
}
