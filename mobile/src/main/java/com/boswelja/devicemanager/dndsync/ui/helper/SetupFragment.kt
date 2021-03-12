package com.boswelja.devicemanager.dndsync.ui.helper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.boswelja.devicemanager.R

internal class SetupFragment : Fragment() {

    private val viewModel: SetupViewModel by viewModels()

    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel: SetupViewModel = viewModel()
                var isLoading by mutableStateOf(false)
                Scaffold(
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = {
                                Icon(Icons.Outlined.NavigateNext, null)
                                Text(stringResource(R.string.button_next))
                            },
                            onClick = {
                                if (!isLoading) {
                                    isLoading = true
                                    viewModel.requestCheckPermission()
                                }
                            }
                        )
                    }
                ) {
                    Column(Modifier.fillMaxSize()) {
                        Text(
                            stringResource(R.string.dnd_sync_helper_setup_title),
                            style = MaterialTheme.typography.h4
                        )
                        if (isLoading) {
                            LinearProgressIndicator()
                        }
                        SetupInstructions()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.onNotiPolicyAccessGranted.observe(viewLifecycleOwner) {
            if (it != null) {
                val action = SetupFragmentDirections.toResultFragment(it)
                findNavController().navigate(action)
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun SetupInstructions() {
        val setupInstructions = stringArrayResource(R.array.interrupt_filter_sync_to_watch_steps)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(setupInstructions) { instruction ->
                ListItem(
                    text = {
                        Text(instruction)
                    }
                )
            }
        }
    }
}
