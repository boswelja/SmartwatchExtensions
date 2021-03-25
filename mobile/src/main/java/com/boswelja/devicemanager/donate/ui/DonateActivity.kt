package com.boswelja.devicemanager.donate.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
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
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.billingclient.api.SkuDetails
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.UpNavigationAppBar
import kotlinx.coroutines.launch

class DonateActivity : AppCompatActivity() {

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scaffoldState = rememberScaffoldState()
            val viewModel: DonateViewModel = viewModel()
            val hasDonated = viewModel.onDonated.observeAsState()

            AppTheme {
                Scaffold(scaffoldState = scaffoldState) {
                    Column(Modifier.fillMaxSize()) {
                        UpNavigationAppBar(onNavigateUp = { finish() })
                        DonateHeader()
                        DonateOptions()
                    }
                    hasDonated.value?.let {
                        if (it) {
                            DonationCompleteSnackbar(scaffoldState)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DonationCompleteSnackbar(scaffoldState: ScaffoldState) {
        val scope = rememberCoroutineScope()
        val text = stringResource(id = R.string.donate_complete)
        scope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                text,
                null
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DonateHeader() {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .aspectRatio(3f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                tint = colorResource(id = R.color.iconTint)
            )
            Text(
                stringResource(id = R.string.donate_thank_you),
                Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center
            )
        }
    }

    @ExperimentalMaterialApi
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

    @ExperimentalMaterialApi
    @Composable
    fun DonateList(donateOptions: List<SkuDetails>, onClick: (SkuDetails) -> Unit) {
        LazyColumn {
            items(donateOptions) { donateOption ->
                DonateItem(donateOption, onClick)
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun DonateOptions() {
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
                0 -> viewModel.recurringDonations.observeAsState()
                else -> viewModel.oneTimeDonations.observeAsState()
            }
            options.value?.let { skuDetails ->
                DonateList(
                    donateOptions = skuDetails,
                    onClick = { viewModel.launchBillingFlow(this@DonateActivity, it) }
                )
            }
        }
    }
}
