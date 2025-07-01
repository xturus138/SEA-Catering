package com.example.seacatering.ui.meal

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seacatering.R
import com.example.seacatering.adapter.ListMealdetailAdapter
import com.example.seacatering.databinding.FragmentMealDetailBinding
import com.example.seacatering.model.Meals
import com.example.seacatering.ui.subscription.SubscriptionFragment
import com.example.seacatering.utils.BottomVisibilityController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MealDetailFragment : Fragment() {

    private lateinit var binding: FragmentMealDetailBinding
    private lateinit var adapter: ListMealdetailAdapter
    private lateinit var bottomNavHelper: BottomVisibilityController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMealDetailBinding.inflate(inflater, container, false)

        adapter = ListMealdetailAdapter(emptyList())
        binding.rvMealDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMealDetail.adapter = adapter

        displayMealDetails()

        binding.backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.subscribeButton.setOnClickListener {
            val meal = arguments?.getParcelable<Meals>("meal_data")
            meal?.let {
                val bundle = Bundle().apply {
                    putParcelable("meal_data", it)
                }

                val subscriptionFragment = SubscriptionFragment().apply {
                    arguments = bundle
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, subscriptionFragment)
                    .addToBackStack(null)
                    .commit()
            }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
        (activity as? BottomVisibilityController)?.setBottomNavVisible(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? BottomVisibilityController)?.setBottomNavVisible(true)
    }

    private fun displayMealDetails() {
        val meal = arguments?.getParcelable<Meals>("meal_data")
        meal?.let {


            adapter.updateData(it.menuList)
        } ?: run {

        }
    }
}
