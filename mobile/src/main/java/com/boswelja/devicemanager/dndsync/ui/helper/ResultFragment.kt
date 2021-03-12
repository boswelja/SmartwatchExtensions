package com.boswelja.devicemanager.dndsync.ui.helper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.boswelja.devicemanager.R

internal class ResultFragment : Fragment() {

    private val args: ResultFragmentArgs by navArgs()
    private val viewModel: ResultViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Scaffold(
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = {
                                Icon(Icons.Outlined.Check, null)
                                Text(stringResource(R.string.button_finish))
                            },
                            onClick = { activity?.finish() }
                        )
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) {
                    ResultStatus(args.success)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setSyncToWatch(args.success)
    }

    @Composable
    fun ResultStatus(wasSuccessful: Boolean) {
        val icon: ImageVector
        val title: String
        val desc: String
        if (wasSuccessful) {
            icon = Icons.Outlined.Check
            title = stringResource(R.string.dnd_sync_helper_success_title)
            desc = stringResource(R.string.dnd_sync_helper_success_message)
        } else {
            icon = Icons.Outlined.ErrorOutline
            title = stringResource(R.string.dnd_sync_helper_failed_title)
            desc = stringResource(R.string.dnd_sync_helper_failed_message)
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                null,
                Modifier.size(180.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.h4
            )
            Text(
                desc,
                style = MaterialTheme.typography.h5
            )
        }
    }
}
