package com.example.seacatering.ui.subscription

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.data.repository.SubscriptionRepository
import com.example.seacatering.model.Subscription
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val subscriptionRepository = SubscriptionRepository()
    private val dataStoreManager = DataStoreManager(application.applicationContext)
    private val auth = FirebaseAuth.getInstance()

    private val _submissionResult = MutableLiveData<Boolean>()
    val submissionResult: LiveData<Boolean> get() = _submissionResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _userPhoneNumber = MutableLiveData<String?>()
    val userPhoneNumber: LiveData<String?> get() = _userPhoneNumber

    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> get() = _totalPrice


    private var lastCalculatedPrice: Double = 0.0

    init {
        fetchUserPhoneNumber()
    }

    private fun fetchUserPhoneNumber() {
        viewModelScope.launch {
            dataStoreManager.userData.firstOrNull()?.let { user ->
                _userPhoneNumber.value = user.noHp
            }
        }
    }

    fun calculateTotalPrice(
        planPriceString: String,
        numberOfMealTypes: Int,
        numberOfDeliveryDays: Int
    ) {
        val planPrice = planPriceString.replace("Rp", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0
        val calculatedPrice = planPrice * numberOfMealTypes * numberOfDeliveryDays * 4.3
        _totalPrice.value = calculatedPrice
        lastCalculatedPrice = calculatedPrice
    }

    fun submitSubscription(
        allergies: String,
        mealType: String,
        deliveryDays: String,
        planId: String,
        phoneNumber: String
    ) {
        val userUid = auth.currentUser?.uid
        if (userUid == null) {
            _errorMessage.value = "User not logged in."
            _submissionResult.value = false
            return
        }

        val endDate = Timestamp(System.currentTimeMillis() / 1000 + (30 * 24 * 60 * 60), 0)


        val newSubscription = Subscription(
            allergies = allergies,
            delivery_days = deliveryDays,
            end_date = endDate,
            meal_type = mealType,
            phone_number = phoneNumber,
            plan_id = planId,
            status = "ACTIVE",
            user_uid = userUid,
            total_price = lastCalculatedPrice
        )

        viewModelScope.launch {
            val result = subscriptionRepository.saveSubscription(newSubscription)
            _submissionResult.value = result
            if (!result) {
                _errorMessage.value = "Failed to save subscription. Please try again."
            }
        }
    }
}