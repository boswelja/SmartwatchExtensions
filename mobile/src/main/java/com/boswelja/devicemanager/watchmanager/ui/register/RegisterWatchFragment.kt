package com.boswelja.devicemanager.watchmanager.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.LifecycleAwareTimer
import com.boswelja.devicemanager.common.ui.adapter.WatchAdapter
import com.boswelja.devicemanager.databinding.FragmentRegisterWatchBinding
import com.boswelja.devicemanager.watchmanager.item.Watch

class RegisterWatchFragment : Fragment() {

    private val viewModel: RegisterWatchViewModel by activityViewModels()
    private val adapter: WatchAdapter by lazy { WatchAdapter(null) }
    private val availableWatchUpdateTimer = LifecycleAwareTimer(TIMER_UPDATE_SECONDS) {
        viewModel.refreshData()
    }

    private lateinit var binding: FragmentRegisterWatchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterWatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeAvailableWatches()

        binding.registeredWatchesRecyclerview.adapter = adapter

        viewModel.registeredWatches.observe(viewLifecycleOwner) {
            binding.finishButton.isEnabled = it.isNotEmpty()
            binding.noWatchesText.isVisible = it.isEmpty()
            adapter.submitList(it)
        }

        lifecycle.addObserver(availableWatchUpdateTimer)

        binding.finishButton.setOnClickListener {
            viewModel.onFinished.fire()
        }
    }

    private fun observeAvailableWatches() {
        viewModel.availableWatches.observe(viewLifecycleOwner) {
            it.forEach { watch ->
                viewModel.registerWatch(watch)
            }
        }
    }

    companion object {
        private const val TIMER_UPDATE_SECONDS: Long = 5
    }
}

@ExperimentalMaterialApi
@Composable
fun RegisterWatchesScreen(registeredWatches: List<Watch>?) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.register_watch_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        Text(
            stringResource(R.string.register_watch_desc),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 16.dp))
        if (registeredWatches.isNullOrEmpty()) {
            Text(
                stringResource(R.string.register_watch_no_watches),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(
                Modifier.padding(16.dp)
            ) {
                items(registeredWatches) { watch ->
                    ListItem(
                        text = { Text(watch.name) },
                        icon = {
                            Icon(
                                Icons.Outlined.Watch,
                                null,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}
