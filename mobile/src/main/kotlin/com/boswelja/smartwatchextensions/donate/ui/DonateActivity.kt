package com.boswelja.smartwatchextensions.donate.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.billingclient.api.SkuDetails
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import com.boswelja.smartwatchextensions.donate.DonationClient
import kotlinx.coroutines.launch

private const val HEADER_ASPECT_RATIO = 3f

/**
 * An Activity to accept donations.
 */
class DonateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scaffoldState = rememberScaffoldState()

            AppTheme {
                Scaffold(
                    topBar = {
                        UpNavigationAppBar(onNavigateUp = { finish() })
                    },
                    scaffoldState = scaffoldState
                ) {
                    DonateScreen(
                        modifier = Modifier.fillMaxSize(),
                        onShowSnackbar = {
                            scaffoldState.snackbarHostState.showSnackbar(it)
                        },
                        onDonate = { client, sku ->
                            client.tryDonate(this, sku)
                        }
                    )
                }
            }
        }
    }
}

/**
 * A Composable screen for displaying donation options.
 * @param modifier [Modifier].
 * @param onShowSnackbar Called when a snackbar needs to be displayed.
 * @param onDonate Called when the user donates.
 */
@Composable
fun DonateScreen(
    modifier: Modifier = Modifier,
    onShowSnackbar: suspend (String) -> Unit,
    onDonate: suspend (DonationClient, SkuDetails) -> Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: DonateViewModel = viewModel()

    val isReady by viewModel.clientConnected.collectAsState(false)

    Column(modifier) {
        DonateHeader()
        if (!isReady) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        DonateOptions { sku ->
            scope.launch {
                val success = onDonate(viewModel.donationClient, sku)
                if (success) {
                    onShowSnackbar(context.getString(R.string.donate_complete))
                }
            }
        }
    }
}

/**
 * A Composable to display a header regarding donations.
 */
@Composable
fun DonateHeader() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .aspectRatio(HEADER_ASPECT_RATIO),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
        )
        Text(
            stringResource(id = R.string.donate_thank_you),
            Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * A Composable for displaying a donation option.
 * @param skuDetails The donation details.
 * @param onClick Called when the donation item is clicked.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DonateItem(skuDetails: SkuDetails, onClick: (SkuDetails) -> Unit) {
    val icon = if (skuDetails.sku.contains("large"))
        painterResource(id = R.drawable.ic_donate_large)
    else
        painterResource(id = R.drawable.ic_donate_small)
    ListItem(
        text = { Text(skuDetails.price) },
        secondaryText = { Text(skuDetails.description) },
        icon = {
            Icon(icon, null)
        },
        modifier = Modifier.clickable { onClick(skuDetails) }
    )
}

/**
 * A Composable for displaying a list of donation options.
 * @param donateOptions The list of options to display.
 * @param onClick Called when the donation item is clicked.
 */
@Composable
fun DonateList(donateOptions: List<SkuDetails>, onClick: (SkuDetails) -> Unit) {
    if (donateOptions.isNotEmpty()) {
        LazyColumn {
            items(donateOptions) { donateOption ->
                DonateItem(donateOption, onClick)
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.donate_fetching_options),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * A Composable for displaying all donation options.
 * @param onDonationClick Called when the user clicks a donation option.
 */
@Composable
fun DonateOptions(
    onDonationClick: (SkuDetails) -> Unit
) {
    val viewModel: DonateViewModel = viewModel()
    val titles = listOf("Monthly", "One-Time")
    var tabIndex by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = tabIndex,
            backgroundColor = MaterialTheme.colors.background
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }
        val options = when (tabIndex) {
            0 -> viewModel.recurringDonations.collectAsState(emptyList())
            else -> viewModel.oneTimeDonations.collectAsState(emptyList())
        }
        options.value?.let { skuDetails ->
            DonateList(
                donateOptions = skuDetails,
                onClick = onDonationClick
            )
        }
    }
}
