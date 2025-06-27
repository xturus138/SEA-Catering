package com.example.seacatering.ui.home

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.seacatering.R
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.data.repository.HomeRepository
import com.example.seacatering.databinding.FragmentHomeBinding
import com.example.seacatering.ui.meal.MealFragment
import com.example.seacatering.ui.profile.ProfileFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    private lateinit var homeRepository: HomeRepository
    private lateinit var homeViewModel: HomeViewModel


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        homeViewModel.fetchUserLocation()

        homeViewModel.locationName.observe(viewLifecycleOwner) { locationName ->
            binding.textView2.text = locationName
        }

        binding.cardView.setOnClickListener(){
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        homeRepository = HomeRepository()
        loadRecommendedItems()
        clickToNavigate()
        return binding.root
    }

    private fun clickToNavigate(){
        binding.linearLayoutRecommended.setOnClickListener() {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MealFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadRecommendedItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (!isAdded) return@launch

            binding.progressBarRecommended.visibility = View.VISIBLE
            hideRecommendedItems()

            val adds = homeRepository.getAddsItems()
            val recommendedItems = homeRepository.getRecommendedItems()

            context?.let { ctx ->
                Glide.with(ctx)
                    .load(adds.getOrNull(0))
                    .placeholder(R.drawable.placeholder_banner)
                    .error(R.drawable.placeholder_banner)
                    .dontAnimate()
                    .into(binding.imgAdds)

                Glide.with(ctx).load(recommendedItems.getOrNull(0)?.photoResId).into(binding.imgRecommended1)
                binding.titleOne.text = recommendedItems.getOrNull(0)?.title ?: ""

                Glide.with(ctx).load(recommendedItems.getOrNull(1)?.photoResId).into(binding.imgRecommended2)
                binding.titleTwo.text = recommendedItems.getOrNull(1)?.title ?: ""

                Glide.with(ctx).load(recommendedItems.getOrNull(2)?.photoResId).into(binding.imgRecommended3)
                binding.titleThird.text = recommendedItems.getOrNull(2)?.title ?: ""
            }

            binding.progressBarRecommended.visibility = View.GONE
            showRecommendedItems()
        }
    }


    private fun hideRecommendedItems() {
        binding.linearLayoutOffersStatic.visibility = View.GONE
        binding.textView3.visibility = View.GONE
        binding.textView4.visibility = View.GONE
        binding.textView5.visibility = View.GONE
        binding.linearLayoutRecommended.visibility = View.GONE
    }

    private fun showRecommendedItems() {
        binding.linearLayoutOffersStatic.visibility = View.VISIBLE
        binding.textView3.visibility = View.VISIBLE
        binding.textView4.visibility = View.VISIBLE
        binding.textView5.visibility = View.VISIBLE
        binding.linearLayoutRecommended.visibility = View.VISIBLE

    }
}