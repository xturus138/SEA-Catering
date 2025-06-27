package com.example.seacatering.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seacatering.R
import com.example.seacatering.databinding.ItemSubscriptionBinding
import com.example.seacatering.model.Subscription
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SubscriptionAdapter(
    private var subscriptions: List<Subscription>,
    private val onPauseClick: (Subscription) -> Unit,
    private val onResumeClick: (Subscription) -> Unit,
    private val onCancelClick: (Subscription) -> Unit
) : RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder>() {

    fun updateSubscriptions(newSubscriptions: List<Subscription>) {
        subscriptions = newSubscriptions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val binding = ItemSubscriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubscriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        val subscription = subscriptions[position]
        holder.bind(subscription, onPauseClick, onResumeClick, onCancelClick)
    }

    override fun getItemCount(): Int = subscriptions.size

    class SubscriptionViewHolder(private val binding: ItemSubscriptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            subscription: Subscription,
            onPauseClick: (Subscription) -> Unit,
            onResumeClick: (Subscription) -> Unit,
            onCancelClick: (Subscription) -> Unit
        ) {
            binding.tvPlanName.text = subscription.plan_name
            binding.tvMealType.text = "Meal Type: ${subscription.meal_type}"
            binding.tvDeliveryDays.text = "Delivery Days: ${formatDeliveryDays(subscription.delivery_days)}"

            subscription.end_date?.let {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.tvEndDate.text = "Ends on: ${dateFormat.format(it.toDate())}"
            } ?: run {
                binding.tvEndDate.text = "Ends on: N/A"
            }

            binding.tvStatus.text = subscription.status
            when (subscription.status) {
                "ACTIVE" -> binding.tvStatus.setBackgroundResource(R.color.green)
                "PAUSED" -> binding.tvStatus.setBackgroundResource(R.color.blueDark)
                "CANCELED" -> binding.tvStatus.setBackgroundResource(R.color.orangeRed)
                else -> binding.tvStatus.setBackgroundResource(R.color.grayBlack)
            }


            if (subscription.pause_periode_start != null && subscription.pause_periode_end != null) {
                val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                val start = dateFormat.format(subscription.pause_periode_start.toDate())
                val end = dateFormat.format(subscription.pause_periode_end.toDate())
                binding.tvPausePeriod.text = "Paused: $start - $end"
                binding.tvPausePeriod.visibility = View.VISIBLE
            } else {
                binding.tvPausePeriod.visibility = View.GONE
            }


            when (subscription.status) {
                "ACTIVE" -> {
                    binding.btnPause.visibility = View.VISIBLE
                    binding.btnResume.visibility = View.GONE
                    binding.btnCancel.visibility = View.VISIBLE
                }
                "PAUSED" -> {
                    binding.btnPause.visibility = View.GONE
                    binding.btnResume.visibility = View.VISIBLE
                    binding.btnCancel.visibility = View.VISIBLE
                }
                else -> {
                    binding.btnPause.visibility = View.GONE
                    binding.btnResume.visibility = View.GONE
                    binding.btnCancel.visibility = View.GONE
                }
            }

            binding.btnPause.setOnClickListener { onPauseClick(subscription) }
            binding.btnResume.setOnClickListener { onResumeClick(subscription) }
            binding.btnCancel.setOnClickListener { onCancelClick(subscription) }
        }

        private fun formatDeliveryDays(days: String): String {
            val dayMap = mapOf(
                "1" to "Mon", "2" to "Tue", "3" to "Wed",
                "4" to "Thu", "5" to "Fri", "6" to "Sat", "7" to "Sun"
            )
            return days.split(",")
                .mapNotNull { dayMap[it.trim()] }
                .joinToString(", ")
        }
    }
}