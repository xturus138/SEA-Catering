package com.example.seacatering.ui.meal


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.seacatering.data.repository.MealRepository
import com.example.seacatering.model.Meals
import kotlinx.coroutines.launch

class MealViewModel : ViewModel() {

    private val repository = MealRepository()

    private val _meals = MutableLiveData<List<Meals>>()
    val meals: LiveData<List<Meals>> get() = _meals

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        fetchMeals()
    }

    private fun fetchMeals() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getMeals()
            _meals.value = result
            _isLoading.value = false
        }
    }
}