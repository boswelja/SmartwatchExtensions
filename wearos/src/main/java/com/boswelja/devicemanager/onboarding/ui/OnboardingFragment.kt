package com.boswelja.devicemanager.onboarding.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.boswelja.devicemanager.R

class OnboardingFragment : Fragment() {

    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                OnboardingFlow()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.onWatchRegistered.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.to_extensionsFragment)
        }
    }
}

@Composable
fun OnboardingFlow() {
    LazyColumn {
        item {
            WelcomeScreen()
        }
        item {
            PhoneInstallInstructions()
        }
        item {
            PhoneSetupInstructions()
        }
    }
}

@Composable
fun WelcomeScreen() {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            context.packageManager.getApplicationIcon(context.packageName)
                .toBitmap().asImageBitmap(),
            null,
            Modifier.size(48.dp)
        )
        Text(
            stringResource(R.string.welcome_to_text),
            style = MaterialTheme.typography.body2
        )
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.h6
        )
        DownArrow()
    }
}

@Composable
fun PhoneInstallInstructions() {
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        Text(
            stringResource(R.string.setup_phone_helper_text),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )
        DownArrow()
    }
}

@Composable
fun PhoneSetupInstructions() {
    val viewModel: OnboardingViewModel = viewModel()
    val watchName by viewModel.setupNameText.observeAsState()
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        Text(
            stringResource(R.string.setup_instructions_text),
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
        Text(
            watchName ?: "Error",
            style = MaterialTheme.typography.h6
        )
        Text(
            stringResource(R.string.setup_hint_text),
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ColumnScope.DownArrow() {
    Box(
        Modifier.weight(1f),
        contentAlignment = Alignment.BottomCenter
    ) {
        Icon(
            Icons.Outlined.ArrowDownward,
            stringResource(R.string.content_description_scroll_down),
            Modifier.size(24.dp)
        )
    }
}
