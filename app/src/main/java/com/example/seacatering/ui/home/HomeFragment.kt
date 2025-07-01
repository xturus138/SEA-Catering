package com.example.seacatering.ui.home

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seacatering.R
import com.example.seacatering.adapter.TestimonialAdapter
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.data.repository.HomeRepository
import com.example.seacatering.databinding.FragmentHomeBinding
import com.example.seacatering.ui.bottombs.InfoBottomSheet
import com.example.seacatering.ui.meal.MealFragment
import com.example.seacatering.ui.profile.ProfileFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeRepository: HomeRepository
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeTestimonialViewModel: HomeTestimonialViewModel
    private lateinit var testimonialAdapter: TestimonialAdapter
    private lateinit var dataStoreManager: DataStoreManager
    private var isRecommendedLoaded = false


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        homeTestimonialViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[HomeTestimonialViewModel::class.java]
        dataStoreManager = DataStoreManager(requireContext().applicationContext)

        homeViewModel.fetchUserLocation()
        homeViewModel.locationName.observe(viewLifecycleOwner) { binding.textView2.text = it }

        binding.btnInfo.setOnClickListener {
            InfoBottomSheet().show(parentFragmentManager, null)
        }

        binding.cardView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        homeRepository = HomeRepository()
        setupTestimonialRecyclerView()
        loadUserProfileImage()
        loadRecommendedAndTestimonials()
        clickToNavigate()

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadRecommendedAndTestimonials()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeTestimonials()
        observeTestimonialLoading()
    }

    private fun clickToNavigate() {
        binding.linearLayoutRecommended.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MealFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadRecommendedAndTestimonials() {
        lifecycleScope.launch {
            binding.progressBarRecommended.visibility = View.VISIBLE
            hideRecommendedItems()

            val adds = homeRepository.getAddsItems()
            val recommendedItems = homeRepository.getRecommendedItems()

            context?.let { ctx ->
                Glide.with(ctx).load(adds.getOrNull(0)).placeholder(R.drawable.placeholder_banner).error(R.drawable.placeholder_banner).into(binding.imgAdds)
                Glide.with(ctx).load(recommendedItems.getOrNull(0)?.photoResId).into(binding.imgRecommended1)
                binding.titleOne.text = recommendedItems.getOrNull(0)?.title.orEmpty()
                Glide.with(ctx).load(recommendedItems.getOrNull(1)?.photoResId).into(binding.imgRecommended2)
                binding.titleTwo.text = recommendedItems.getOrNull(1)?.title.orEmpty()
                Glide.with(ctx).load(recommendedItems.getOrNull(2)?.photoResId).into(binding.imgRecommended3)
                binding.titleThird.text = recommendedItems.getOrNull(2)?.title.orEmpty()
            }

            isRecommendedLoaded = true
            binding.progressBarRecommended.visibility = View.GONE
            showRecommendedItems()

            if (homeTestimonialViewModel.testimonials.value.isNullOrEmpty()) {
                binding.rvReview.visibility = View.GONE
                binding.progressBarRv.visibility = View.VISIBLE
            }
        }
    }


    private fun loadUserProfileImage() {
        lifecycleScope.launch {
            dataStoreManager.userData.collectLatest { userData ->
                val imageUrl = userData?.profileImageUrl
                if (!imageUrl.isNullOrEmpty()) {
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
            }
        }
    }

    private fun setupTestimonialRecyclerView() {
        testimonialAdapter = TestimonialAdapter(emptyList())
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvReview.layoutManager = layoutManager
        binding.rvReview.adapter = testimonialAdapter

        binding.rvReview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (homeTestimonialViewModel.isLoadingTestimonials.value == false && lastVisibleItem >= totalItemCount - 1) {
                    homeTestimonialViewModel.displayNextPage()
                }
            }
        })
    }

    private fun observeTestimonials() {
        homeTestimonialViewModel.testimonials.observe(viewLifecycleOwner) {
            testimonialAdapter.updateData(it)
            if (isRecommendedLoaded) {
                binding.progressBarRv.visibility = View.GONE
                binding.rvReview.visibility = View.VISIBLE
            }
        }
    }


    private fun observeTestimonialLoading() {
        homeTestimonialViewModel.isLoadingTestimonials.observe(viewLifecycleOwner) { isLoading ->
            if (isRecommendedLoaded) {
                binding.progressBarRv.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
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
