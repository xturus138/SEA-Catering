package com.example.seacatering.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seacatering.data.repository.TestimonialRepository
import com.example.seacatering.model.Testimonial
import kotlinx.coroutines.launch

class HomeTestimonialViewModel(application: Application) : AndroidViewModel(application) {

    private val testimonialRepository = TestimonialRepository()

    private val _testimonials = MutableLiveData<List<Testimonial>>()
    val testimonials: LiveData<List<Testimonial>> get() = _testimonials

    private val _isLoadingTestimonials = MutableLiveData<Boolean>()
    val isLoadingTestimonials: LiveData<Boolean> get() = _isLoadingTestimonials

    private var allTestimonials: List<Testimonial> = emptyList()
    private var currentIndex = 0
    private val PAGE_SIZE = 5

    init {
        loadInitialTestimonials()
    }

    private fun loadInitialTestimonials() {
        _isLoadingTestimonials.value = true
        viewModelScope.launch {
            allTestimonials = testimonialRepository.getAllTestimonials()
            allTestimonials = allTestimonials.shuffled()
            displayNextPage()
        }
    }

    fun displayNextPage() {
        if (allTestimonials.isEmpty()) {
            _testimonials.value = emptyList()
            _isLoadingTestimonials.value = false
            return
        }

        val start = currentIndex
        val end = (currentIndex + PAGE_SIZE).coerceAtMost(allTestimonials.size)
        val newReviews = allTestimonials.subList(start, end)

        if (newReviews.isEmpty()) {
            currentIndex = 0
            displayNextPage()
        } else {
            _testimonials.value = newReviews
            currentIndex = end
            _isLoadingTestimonials.value = false
        }
    }

    fun refreshAllTestimonials() {
        _isLoadingTestimonials.value = true
        currentIndex = 0
        viewModelScope.launch {
            allTestimonials = testimonialRepository.getAllTestimonials()
            displayNextPage()
        }
    }
}