package com.boswelja.devicemanager.common.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R

open class LoadingFragment : Fragment() {

    /**
     * The progress displayed to the user. Use 0 to set indeterminate.
     * Works on a range of 0.0 for indeterminate to 1.0 for full progress.
     */
    internal var progress by mutableStateOf(0f)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Loading()
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun Loading() {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (progress == 0.0f) {
                LinearProgressIndicator()
            } else {
                LinearProgressIndicator(progress = progress)
            }
            Text(stringResource(R.string.dnd_sync_helper_loading_text))
        }
    }
}
