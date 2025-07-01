package com.example.seacatering.ui.dashboard.user


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seacatering.R
import com.example.seacatering.adapter.SubscriptionAdapter
import com.example.seacatering.databinding.FragmentResultBinding
import com.example.seacatering.model.Subscription
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import java.util.Date

class ResultFragment : Fragment() {

    private lateinit var binding: FragmentResultBinding
    private lateinit var viewModel: ResultViewModel
    private lateinit var subscriptionAdapter: SubscriptionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(ResultViewModel::class.java)

        setupRecyclerView()
        observeViewModel()

        viewModel.fetchUserSubscriptions()
    }

    private fun setupRecyclerView() {
        subscriptionAdapter = SubscriptionAdapter(
            subscriptions = emptyList(),
            onPauseClick = { subscription -> showPauseDialog(subscription) },
            onResumeClick = { subscription -> showResumeConfirmationDialog(subscription) },
            onCancelClick = { subscription -> showCancelConfirmationDialog(subscription) }
        )
        binding.rvSubscriptions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subscriptionAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.subscriptions.observe(viewLifecycleOwner) { subscriptions ->
            subscriptionAdapter.updateSubscriptions(subscriptions)
            binding.tvNoSubscriptions.visibility = if (subscriptions.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarSubs.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { (success, message) ->
            if (success) {
                Toast.makeText(context, "Operation completed successfully.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Oops, something went wrong! Please contact administrator", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showPauseDialog(subscription: Subscription) {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select pause period")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second

            if (startDate != null && endDate != null) {

                AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Pause")
                    .setMessage("Are you sure you want to pause your subscription from ${formatDate(startDate)} to ${formatDate(endDate)}?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        viewModel.pauseSubscription(subscription, startDate, endDate)
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                Toast.makeText(context, "Please select a valid date range.", Toast.LENGTH_SHORT).show()
            }
        }

        dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun showResumeConfirmationDialog(subscription: Subscription) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Resume")
            .setMessage("Are you sure you want to resume your subscription?")
            .setPositiveButton("Yes") { dialog, _ ->
                viewModel.resumeSubscription(subscription)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showCancelConfirmationDialog(subscription: Subscription) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Cancellation")
            .setMessage("Are you sure you want to permanently cancel your subscription? This action cannot be undone.")
            .setPositiveButton("Yes, Cancel") { dialog, _ ->
                viewModel.cancelSubscription(subscription)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun formatDate(milliseconds: Long): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(Date(milliseconds))
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()

    }
}