package com.example.seacatering.ui.bottombs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.seacatering.databinding.BottomSheetSubscriptionBinding
import com.example.seacatering.model.Subscription
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class SubscriptionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSubscriptionBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_SUBSCRIPTION = "arg_subscription"

        fun newInstance(subscription: Subscription): SubscriptionBottomSheet {
            val fragment = SubscriptionBottomSheet()
            val bundle = Bundle()
            bundle.putParcelable(ARG_SUBSCRIPTION, subscription)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscription = arguments?.getParcelable(ARG_SUBSCRIPTION)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetSubscriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscription?.let { sub ->
            binding.tvPlanName.text = sub.plan_name
            binding.tvMealType.text = "Meal Types: ${sub.meal_type}"
            binding.tvDeliveryDays.text = "Delivery Days: ${formatDeliveryDays(sub.delivery_days)}"
            binding.tvTotalPrice.text = "Total Price: ${formatPrice(sub.total_price)}"
            binding.tvStatus.text = sub.status

            binding.tvAllergies.text = "Allergies: ${sub.allergies ?: "-"}"
            binding.tvPhoneNumber.text = "Phone: ${sub.phone_number ?: "-"}"
            binding.tvCreatedAt.text = "Created: ${formatDate(sub.created_at?.toDate())}"
            binding.tvEndDate.text = "End Date: ${formatDate(sub.end_date?.toDate())}"
            binding.tvCanceledAt.text = "Canceled At: ${formatDate(sub.canceled_at?.toDate())}"

            val bgColor = when (sub.status) {
                "ACTIVE" -> com.example.seacatering.R.color.green
                "PAUSED" -> com.example.seacatering.R.color.blueDark
                "CANCELED" -> com.example.seacatering.R.color.orangeRed
                else -> com.example.seacatering.R.color.grayBlack
            }
            binding.tvStatus.setBackgroundResource(bgColor)
        }
    }

    private fun formatPrice(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(price)
    }

    private fun formatDeliveryDays(days: String): String {
        val dayMap = mapOf(
            "1" to "Mon", "2" to "Tue", "3" to "Wed",
            "4" to "Thu", "5" to "Fri", "6" to "Sat", "7" to "Sun"
        )
        return days.split(",").mapNotNull { dayMap[it.trim()] }.joinToString(", ")
    }

    private fun formatDate(date: java.util.Date?): String {
        return if (date == null) "-" else SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
