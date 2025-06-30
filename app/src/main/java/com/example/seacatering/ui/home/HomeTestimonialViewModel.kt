package com.example.seacatering.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seacatering.data.repository.TestimonialRepository
import com.example.seacatering.model.Testimonial
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeTestimonialViewModel(application: Application) : AndroidViewModel(application) {

    private val testimonialRepository = TestimonialRepository()

    private val _testimonials = MutableLiveData<List<Testimonial>>()
    val testimonials: LiveData<List<Testimonial>> get() = _testimonials

    private val _isLoadingTestimonials = MutableLiveData<Boolean>()
    val isLoadingTestimonials: LiveData<Boolean> get() = _isLoadingTestimonials

    private var allTestimonials: List<Testimonial> = emptyList()
    private var currentIndex = 0

    companion object {
        private const val PAGE_SIZE = 5
    }

    init {
        loadInitialTestimonials()
    }

    private fun loadInitialTestimonials() {
        _isLoadingTestimonials.value = true
        viewModelScope.launch {
            val fetched = testimonialRepository.getAllTestimonials()
            if (isActive) {
                allTestimonials = fetched
                currentIndex = 0
                _testimonials.value = emptyList()
                displayNextPageInternal()
            }
        }
    }

    fun displayNextPage() {
        if (_isLoadingTestimonials.value == true) return
        _isLoadingTestimonials.value = true
        displayNextPageInternal()
    }

    private fun displayNextPageInternal() {
        if (allTestimonials.isEmpty()) {
            _isLoadingTestimonials.value = false
            return
        }

        val start = currentIndex
        val end = (currentIndex + PAGE_SIZE).coerceAtMost(allTestimonials.size)

        val nextItems = allTestimonials.subList(start, end)
        val currentList = _testimonials.value?.toMutableList() ?: mutableListOf()
        currentList.addAll(nextItems)

        _testimonials.value = currentList

        currentIndex = if (end == allTestimonials.size) 0 else end
        _isLoadingTestimonials.value = false
    }
}
