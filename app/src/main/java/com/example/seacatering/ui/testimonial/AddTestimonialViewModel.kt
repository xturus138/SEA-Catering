package com.example.seacatering.ui.testimonial

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.data.repository.AuthRepository
import com.example.seacatering.data.repository.TestimonialRepository
import com.example.seacatering.model.Testimonial
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AddTestimonialViewModel(application: Application) : AndroidViewModel(application) {

    private val testimonialRepository = TestimonialRepository()
    private val authRepository = AuthRepository()
    private val dataStoreManager = DataStoreManager(application.applicationContext)
    private val auth = FirebaseAuth.getInstance()

    private val _submissionResult = MutableLiveData<Boolean>()
    val submissionResult: LiveData<Boolean> get() = _submissionResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _customerName = MutableLiveData<String?>()
    val customerName: LiveData<String?> get() = _customerName

    init {
        fetchCustomerName()
    }

    private fun fetchCustomerName() {
        viewModelScope.launch {
            dataStoreManager.userData.firstOrNull()?.let { user ->
                _customerName.value = user.name
            } ?: run {
                auth.currentUser?.displayName?.let {
                    _customerName.value = it
                }
            }
        }
    }

    fun submitTestimonial(review: String, rating: Float) {
        val userUid = auth.currentUser?.uid
        val name = _customerName.value

        if (userUid == null || name.isNullOrEmpty() || review.isEmpty() || rating == 0f) {
            _errorMessage.value = "Please fill in all fields and provide a rating."
            _submissionResult.value = false
            return
        }

        val newTestimonial = Testimonial(
            customerName = name,
            review = review,
            rating = rating.toInt(),
            user_uid = userUid
        )

        viewModelScope.launch {
            val result = testimonialRepository.saveTestimonial(newTestimonial)
            _submissionResult.value = result
            if (!result) {
                _errorMessage.value = "Failed to submit testimonial. Please try again."
            }
        }
    }
}