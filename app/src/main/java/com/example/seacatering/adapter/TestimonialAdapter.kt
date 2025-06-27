package com.example.seacatering.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.seacatering.databinding.ItemTestimonialBinding
import com.example.seacatering.model.Testimonial

class TestimonialAdapter(private var testimonials: List<Testimonial>) :
    RecyclerView.Adapter<TestimonialAdapter.TestimonialViewHolder>() {

    fun updateData(newTestimonials: List<Testimonial>) {
        testimonials = newTestimonials
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestimonialViewHolder {
        val binding = ItemTestimonialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TestimonialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TestimonialViewHolder, position: Int) {
        holder.bind(testimonials[position])
    }

    override fun getItemCount(): Int = testimonials.size

    class TestimonialViewHolder(private val binding: ItemTestimonialBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(testimonial: Testimonial) {
            binding.tvCustomerName.text = testimonial.customerName
            binding.tvReviewMessage.text = testimonial.review
            binding.ratingBar.rating = testimonial.rating.toFloat()
        }
    }
}