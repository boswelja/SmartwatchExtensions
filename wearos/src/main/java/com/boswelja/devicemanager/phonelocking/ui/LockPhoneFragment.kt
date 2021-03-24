package com.boswelja.devicemanager.phonelocking.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhonelinkLock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.AppTheme

class LockPhoneFragment : Fragment() {

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    val viewModel: LockPhoneViewModel = viewModel()
                    val phoneLockingEnabled by viewModel.phoneLockingEnabled.observeAsState()
                    val phoneName by viewModel.phoneName.observeAsState()
                    PhoneLockingScreen(
                        phoneLockingEnabled == true,
                        phoneName ?: stringResource(R.string.default_phone_name)
                    ) {
                        viewModel.requestLockPhone()
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun PhoneLockingScreen(
    phoneLockingEnabled: Boolean,
    phoneName: String,
    onClick: () -> Unit
) {
    if (phoneLockingEnabled == true) {
        PhoneLockingEnabled(
            phoneName = phoneName,
            onClick = onClick
        )
    } else {
        PhoneLockingDisabled()
    }
}

@ExperimentalMaterialApi
@Composable
fun PhoneLockingDisabled() {
    ListItem(
        text = { Text(stringResource(R.string.lock_phone_disabled)) },
        icon = { Icon(Icons.Outlined.PhonelinkLock, null) }
    )
}

@Composable
fun PhoneLockingEnabled(
    phoneName: String,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.PhonelinkLock,
            null,
            Modifier.size(56.dp)
        )
        Text(
            stringResource(R.string.lock_phone, phoneName),
            style = MaterialTheme.typography.h6
        )
    }
}
