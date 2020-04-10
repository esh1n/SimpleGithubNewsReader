package com.lab.esh1n.github.events.fragment

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.lab.esh1n.github.R
import com.lab.esh1n.github.base.BaseObserver
import com.lab.esh1n.github.base.BaseVMFragment
import com.lab.esh1n.github.databinding.FragmentEventsBinding
import com.lab.esh1n.github.domain.base.ErrorModel
import com.lab.esh1n.github.events.EventModel
import com.lab.esh1n.github.events.EventsAdapter
import com.lab.esh1n.github.events.viewmodel.EventsVM
import com.lab.esh1n.github.events.viewmodel.SharedEventViewModel
import com.lab.esh1n.github.utils.SnackbarBuilder
import com.lab.esh1n.github.utils.addFragmentToStack
import com.lab.esh1n.github.utils.setVisibleOrGone

/**
 * Created by esh1n on 3/16/18.
 */

class EventsFragment : BaseVMFragment<EventsVM>() {
    override val viewModelClass = EventsVM::class.java

    override val layoutResource = R.layout.fragment_events

    private var binding: FragmentEventsBinding? = null

    private lateinit var adapter: EventsAdapter

    private lateinit var sharedEventViewModel: SharedEventViewModel


    private val isPortraitMode: Boolean
            by lazy {
                requireActivity().findViewById<View>(R.id.fragment_events) == null
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.startPeriodicSync()
    }

    override fun setupView(rootView: View) {
        super.setupView(rootView)
        binding = DataBindingUtil.bind(rootView)
        binding?.let {
            adapter = EventsAdapter(this::onEventClick)
            it.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
            it.recyclerview.setHasFixedSize(true)
            it.recyclerview.adapter = adapter
            it.swipeRefreshLayout.setOnRefreshListener {
                viewModel.refresh()
            }
        }

    }

    private fun onEventClick(eventModel: EventModel) {
        sharedEventViewModel.eventId.postValue(eventModel.id)
        if (isPortraitMode) {
            activity.addFragmentToStack(EventDetailFragment.newInstance())
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sharedEventViewModel = ViewModelProviders.of(requireActivity()).get(SharedEventViewModel::class.java)
        observeEvents()
        viewModel.loadEvents()
    }

    private fun observeEvents() {

        viewModel.events.observe(this, object : BaseObserver<List<EventModel>>() {
            override fun onData(data: List<EventModel>?) {

                val isEmpty = data?.isEmpty() ?: true
                val emptyView = binding?.viewEmpty!!
                emptyView.setVisibleOrGone(isEmpty)
                if (isEmpty) {
                    emptyView.text = getString(R.string.text_no_events)
                }
                adapter.swapEvents(data ?: emptyList())
                (binding?.recyclerview?.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)

            }

            override fun onError(error: ErrorModel?) {
                SnackbarBuilder.buildErrorSnack(view!!, error?.message!!).show()
            }

            override fun onProgress(progress: Boolean) {
                super.onProgress(progress)
                showProgress(progress)
            }
        })
        viewModel.refreshOperation.observe(this, object : BaseObserver<Unit>() {
            override fun onData(data: Unit?) {
                data?.let {
                    SnackbarBuilder.buildSnack(view!!, getString(R.string.text_events_updated_successfully)).show()
                }
            }

            override fun onError(error: ErrorModel?) {
                SnackbarBuilder.buildErrorSnack(view!!, getString(R.string.error_unexpected)).show()
            }

            override fun onProgress(progress: Boolean) {
                super.onProgress(progress)
                showProgress(progress)
            }

        })

    }

    private fun showProgress(progress: Boolean) {
        if (progress) {
            showLoading()
        } else {
            hideLoading()
        }
    }

    private fun showLoading() {
        binding?.let {
            if (!it.swipeRefreshLayout.isRefreshing) {
                it.loadingIndicator.setVisibleOrGone(true)
            }
        }

    }

    private fun hideLoading() {
        binding?.let {
            if (it.swipeRefreshLayout.isRefreshing) {
                it.swipeRefreshLayout.isRefreshing = false
            } else {
                it.loadingIndicator.setVisibleOrGone(false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedEventViewModel.eventId.value = -1
    }

    companion object {
        fun newInstance() = EventsFragment()
    }

}