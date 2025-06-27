package com.example.seacatering.ui.meal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seacatering.R
import com.example.seacatering.adapter.ListMealplanAdapter
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.databinding.FragmentMealBinding
import com.example.seacatering.model.Meals
import com.example.seacatering.ui.subscription.SubscriptionFragment
import kotlinx.coroutines.launch

class MealFragment : Fragment(), ListMealplanAdapter.onMealClickListener {

    private lateinit var binding: FragmentMealBinding
    private lateinit var viewModel: MealViewModel
    private lateinit var adapter: ListMealplanAdapter

    private var selectedMeal: Meals? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMealBinding.inflate(inflater, container, false)

        binding.rvMealPlan.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.updateButton.visibility = View.GONE
        binding.textView14.visibility = View.GONE

        viewModel = ViewModelProvider(this)[MealViewModel::class.java]
        adapter = ListMealplanAdapter(emptyList(), this)
        binding.rvMealPlan.adapter = adapter

        observeViewModel()

        binding.updateButton.setOnClickListener {
            selectedMeal?.let { meal ->
                val bundle = Bundle().apply {
                    putParcelable("meal_data", meal)
                }
                val detailFragment = MealDetailFragment().apply {
                    arguments = bundle
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        return binding.root
    }

    private fun observeViewModel() {
        viewModel.meals.observe(viewLifecycleOwner) { meals ->
            adapter.updateData(meals)
            binding.updateButton.visibility = if (meals.isNotEmpty()) View.VISIBLE else View.GONE
            binding.textView14.visibility = if (meals.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }


    override fun onMealClick(meal: Meals) {
        selectedMeal = meal
    }
}


