package com.example.seacatering.ui.dashboard.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.seacatering.R
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.databinding.FragmentAdminDashboardBinding
import com.example.seacatering.model.Role
import com.example.seacatering.ui.AuthActivity
import com.example.seacatering.utils.BottomVisibilityController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminDashboardFragment : Fragment() {

    private lateinit var binding: FragmentAdminDashboardBinding
    private lateinit var viewModel: AdminDashboardViewModel
    private lateinit var dataStoreManager: DataStoreManager

    private var selectedStartDate: Timestamp? = null
    private var selectedEndDate: Timestamp? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataStoreManager = DataStoreManager(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(AdminDashboardViewModel::class.java)

        checkAdminRoleAndLoadData()
        setupDateRangeSelector()
        observeViewModel()

        binding.btnLogoutAdmin.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun checkAdminRoleAndLoadData() {
        lifecycleScope.launch {
            val user = dataStoreManager.userData.first()
            if (user?.role == Role.ADMIN) {
                binding.dashboardGridLayout.visibility = View.VISIBLE
                binding.progressBarAdmin.visibility = View.GONE
                binding.btnLogoutAdmin.visibility = View.VISIBLE
            } else {
                binding.dashboardGridLayout.visibility = View.GONE
                binding.progressBarAdmin.visibility = View.GONE
                binding.tvErrorMessageAdmin.visibility = View.VISIBLE
                binding.tvErrorMessageAdmin.text = "Access Denied: You must be an administrator to view this dashboard."
                binding.tvAdminDashboardTitle.text = "Access Denied"
                binding.viewLineAdmin.visibility = View.GONE
                binding.tvDateRangeLabel.visibility = View.GONE
                binding.tvSelectedDateRange.visibility = View.GONE
                binding.btnSelectDateRange.visibility = View.GONE
                binding.btnLogoutAdmin.visibility = View.GONE
                (activity as? BottomVisibilityController)?.setBottomNavVisible(false)
            }
        }
    }

    //maps, export, subscribe list, validation

    private fun setupDateRangeSelector() {
        val calendar = Calendar.getInstance()
        val currentMonthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvSelectedDateRange.text = "Current Month (${currentMonthFormatter.format(calendar.time)})"

        binding.btnSelectDateRange.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .build()

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                val startDate = selection.first
                val endDate = selection.second

                if (startDate != null && endDate != null) {
                    val endCalendar = Calendar.getInstance().apply {
                        timeInMillis = endDate
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }

                    selectedStartDate = Timestamp(Date(startDate))
                    selectedEndDate = Timestamp(endCalendar.time)

                    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    binding.tvSelectedDateRange.text = "${formatter.format(Date(startDate))} - ${formatter.format(Date(endCalendar.timeInMillis))}"

                    viewModel.fetchDashboardData(selectedStartDate!!, selectedEndDate!!)
                } else {
                    Toast.makeText(context, "Please select a valid date range.", Toast.LENGTH_SHORT).show()
                }
            }
            dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER_ADMIN")
        }
    }

    private fun observeViewModel() {
        viewModel.newSubscriptions.observe(viewLifecycleOwner) { count ->
            binding.tvNewSubscriptionsCount.text = count.toString()
        }

        viewModel.monthlyRecurringRevenue.observe(viewLifecycleOwner) { mrr ->
            val formattedMRR = NumberFormat.getCurrencyInstance(Locale("in", "ID")).format(mrr)
            binding.tvMRR.text = formattedMRR.replace("Rp", "Rp ")
        }

        viewModel.canceledSubscriptionsCount.observe(viewLifecycleOwner) { count ->
            binding.tvCanceledSubscriptionsCount.text = count.toString()
        }

        viewModel.subscriptionGrowth.observe(viewLifecycleOwner) { count ->
            binding.tvSubscriptionGrowthCount.text = count.toString()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarAdmin.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.dashboardGridLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.tvErrorMessageAdmin.visibility = View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                binding.tvErrorMessageAdmin.text = message
                binding.tvErrorMessageAdmin.visibility = View.VISIBLE
                binding.dashboardGridLayout.visibility = View.GONE
            } else {
                binding.tvErrorMessageAdmin.visibility = View.GONE
            }
        }

        viewModel.logoutResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(context, "Logout failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? BottomVisibilityController)?.setBottomNavVisible(false)
    }
}