package com.boswelja.devicemanager.donate.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Money
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.SkuDetails

@Composable
fun DonateItem(skuDetails: SkuDetails, onClick: (SkuDetails) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick(skuDetails) })
            .padding(16.dp)
    ) {
        Icon(
            Icons.Outlined.Money,
            skuDetails.price,
            Modifier.size(64.dp)
        )
        Column(Modifier.fillMaxWidth()) {
            Text(skuDetails.price, maxLines = 1)
            Text(skuDetails.description)
        }
    }
}

@Composable
fun DonateList(donateOptions: List<SkuDetails>, onClick: (SkuDetails) -> Unit) {
    LazyColumn {
        items(donateOptions) { donateOption ->
            DonateItem(donateOption, onClick)
        }
    }
}
