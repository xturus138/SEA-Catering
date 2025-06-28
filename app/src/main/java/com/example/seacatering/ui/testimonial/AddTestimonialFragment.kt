package com.example.seacatering.ui.testimonial

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.example.seacatering.R
import com.example.seacatering.databinding.FragmentAddTestimonialBinding
import com.example.seacatering.ui.home.HomeFragment
import com.example.seacatering.ui.meal.MealFragment
import com.example.seacatering.utils.BottomVisibilityController
import com.google.android.material.bottomnavigation.BottomNavigationView

class AddTestimonialFragment : Fragment() {

    private lateinit var binding: FragmentAddTestimonialBinding
    private lateinit var viewModel: AddTestimonialViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTestimonialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(AddTestimonialViewModel::class.java)

        viewModel.customerName.observe(viewLifecycleOwner) { name ->
            binding.inputCustomerName.setText(name)
        }

        binding.submitTestimonialButton.setOnClickListener {
            submitTestimonial()
        }

        viewModel.submissionResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "Testimonial submitted successfully!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()

            } else {
                Toast.makeText(context, viewModel.errorMessage.value ?: "Failed to submit testimonial.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MealFragment())
                .commit()
        }
    }

    private fun submitTestimonial() {
        val review = binding.inputReview.text.toString().trim()
        val rating = binding.ratingBar.rating

        viewModel.submitTestimonial(review, rating)
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)?.visibility = View.VISIBLE
    }
}