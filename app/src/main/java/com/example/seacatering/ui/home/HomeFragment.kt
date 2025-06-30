package com.example.seacatering.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Pastikan Glide sudah diimpor
import com.example.seacatering.R
import com.example.seacatering.data.DataStoreManager // Import DataStoreManager
import com.example.seacatering.data.repository.HomeRepository
import com.example.seacatering.databinding.FragmentHomeBinding
import com.example.seacatering.ui.meal.MealFragment
import com.example.seacatering.ui.profile.ProfileFragment
import com.example.seacatering.adapter.TestimonialAdapter
import kotlinx.coroutines.flow.collectLatest // Import collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.net.Uri // Import Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.example.seacatering.ui.bottombs.InfoBottomSheet

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    private lateinit var homeRepository: HomeRepository
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeTestimonialViewModel: HomeTestimonialViewModel
    private lateinit var testimonialAdapter: TestimonialAdapter
    private lateinit var dataStoreManager: DataStoreManager




    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        homeTestimonialViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))
            .get(HomeTestimonialViewModel::class.java)
        dataStoreManager = DataStoreManager(requireContext().applicationContext)

        homeViewModel.fetchUserLocation()

        homeViewModel.locationName.observe(viewLifecycleOwner) { locationName ->
            binding.textView2.text = locationName
        }

        binding.btnInfo.setOnClickListener(){
            val bottomSheet = InfoBottomSheet()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
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
        setupTestimonialRecyclerView()
        observeTestimonials()
        loadUserProfileImage()





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



    private fun loadUserProfileImage() {
        viewLifecycleOwner.lifecycleScope.launch {
            dataStoreManager.userData.collectLatest { userData ->
                userData?.profileImageUrl?.let { imageUrl ->
                    if (imageUrl.isNotEmpty()) {
                        context?.let { ctx ->
                            Glide.with(ctx)
                                .load(Uri.parse(imageUrl))
                                .circleCrop()
                                .placeholder(R.drawable.ic_default_profile_image)
                                .error(R.drawable.ic_default_profile_image)
                                .into(binding.imgProfile)
                        }
                    } else {
                        binding.imgProfile.setImageResource(R.drawable.ic_default_profile_image)
                    }
                } ?: run {
                    binding.imgProfile.setImageResource(R.drawable.ic_default_profile_image)
                }
            }
        }
    }

    private fun setupTestimonialRecyclerView() {
        binding.progressBarRv.visibility = View.VISIBLE
        testimonialAdapter = TestimonialAdapter(emptyList())
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvReview.layoutManager = layoutManager
        binding.rvReview.adapter = testimonialAdapter


        binding.rvReview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!homeTestimonialViewModel.isLoadingTestimonials.value!! && lastVisibleItem >= totalItemCount - 1) {
                    homeTestimonialViewModel.displayNextPage()
                }
            }
        })
    }

    private fun observeTestimonials() {
        homeTestimonialViewModel.testimonials.observe(viewLifecycleOwner) { testimonials ->
            testimonialAdapter.updateData(testimonials)
            binding.progressBarRv.visibility = View.GONE
        }

    }




    private fun hideRecommendedItems() {
        binding.linearLayoutOffersStatic.visibility = View.GONE
        binding.textView3.visibility = View.GONE
        binding.textView4.visibility = View.GONE
        binding.textView5.visibility = View.GONE
        binding.linearLayoutRecommended.visibility = View.GONE
        binding.rvReview.visibility = View.GONE
    }

    private fun showRecommendedItems() {
        binding.linearLayoutOffersStatic.visibility = View.VISIBLE
        binding.textView3.visibility = View.VISIBLE
        binding.textView4.visibility = View.VISIBLE
        binding.textView5.visibility = View.VISIBLE
        binding.linearLayoutRecommended.visibility = View.VISIBLE
        binding.rvReview.visibility = View.VISIBLE
    }
}