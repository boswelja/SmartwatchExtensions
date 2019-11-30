package com.boswelja.devicemanager.ui.watchsetup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.watchconnectionmanager.Watch
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionService
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class WatchSetupFragment : Fragment() {

    private val watchConnectionManagerConnection = object : WatchConnectionService.Connection() {
        override fun onWatchManagerBound(service: WatchConnectionService) {
            watchConnectionManager = service
            updateAvailableWatches()
        }

        override fun onWatchManagerUnbound() {
            watchConnectionManager = null
        }
    }

    private var watchConnectionManager: WatchConnectionService? = null

    private lateinit var refreshButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var watchSetupRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_watch_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshButton = view.findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener {
            updateAvailableWatches()
        }

        progressBar = view.findViewById(R.id.progress_bar)

        watchSetupRecyclerView = view.findViewById(R.id.watch_setup_recyclerview)
        watchSetupRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = WatchSetupAdapter(this@WatchSetupFragment)
        }

        setLoading(true)

        WatchConnectionService.bind(context!!, watchConnectionManagerConnection)
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unbindService(watchConnectionManagerConnection)
    }

    private fun updateAvailableWatches() {
        if (watchConnectionManager != null) {
            setLoading(true)
            watchConnectionManager!!.getAllConnectedWatches()
                    .addOnSuccessListener {
                        val notRegisteredWatches = ArrayList<Watch>()
                        if (it.isNotEmpty()) {
                            val registeredWatches = watchConnectionManager!!.getAllRegisteredWatches()
                            for (watchNode in it) {
                                if (!registeredWatches.any { watch -> watch.id == watchNode.id }) {
                                    notRegisteredWatches.add(Watch(watchNode))
                                }
                            }
                        }
                        if (notRegisteredWatches.isNotEmpty()) {
                            (watchSetupRecyclerView.adapter as WatchSetupAdapter).setWatches(notRegisteredWatches)
                        } else {
                            //TODO Show no watch message
                        }
                        setLoading(false)
                    }
                    .addOnFailureListener {
                        setLoading(false)
                        //TODO Show error
                    }
        }
    }

    private fun setLoading(loading: Boolean) {
        refreshButton.isEnabled = !loading
        watchSetupRecyclerView.isEnabled = !loading
        progressBar.visibility = if (loading) {
            ProgressBar.VISIBLE
        } else {
            ProgressBar.INVISIBLE
        }
    }

    fun requestRegisterWatch(watch: Watch) {
        MaterialAlertDialogBuilder(context!!).apply {
            background = context.getDrawable(R.drawable.dialog_background)
            setTitle("Add ${watch.name}?")
            setMessage("This will add your ${watch.name} to Wearable Extensions.")
            setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                watchConnectionManager?.addWatch(watch)
                activity?.setResult(WatchSetupActivity.RESULT_WATCH_ADDED)
                activity?.finish()
            }
            setNegativeButton(R.string.dialog_button_no) { dialogInterface, _ ->
                dialogInterface.cancel()
            }
        }.also {
            it.show()
        }
    }
}
