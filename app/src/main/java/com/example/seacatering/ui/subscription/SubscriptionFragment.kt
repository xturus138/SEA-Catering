package com.example.seacatering.ui.subscription

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.seacatering.R
import com.example.seacatering.databinding.FragmentSubscriptionBinding
import com.example.seacatering.model.Meals
import com.example.seacatering.utils.BottomVisibilityController
import com.google.android.material.bottomnavigation.BottomNavigationView

class SubscriptionFragment : Fragment() {

    private lateinit var binding: FragmentSubscriptionBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubscriptionBinding.inflate(layoutInflater, container, false)

        binding.backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val meal = arguments?.getParcelable<Meals>("meal_data")
        meal?.let {
            val name = it.name
            val price = it.price
            val photo = it.imageResId
            binding.inputPlanSelection.setText(name)
        }
        Log.d("SubscriptionFragment", "onViewCreated: Meal Received: $meal")
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