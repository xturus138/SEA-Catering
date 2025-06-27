package com.example.seacatering.ui.dashboard.user

import com.google.firebase.firestore.FieldValue
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
import java.util.Date

class ResultViewModel(application: Application) : AndroidViewModel(application) {

    private val subscriptionRepository = SubscriptionRepository()
    private val dataStoreManager = DataStoreManager(application.applicationContext)
    private val auth = FirebaseAuth.getInstance()

    private val _subscriptions = MutableLiveData<List<Subscription>>()
    val subscriptions: LiveData<List<Subscription>> get() = _subscriptions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _actionResult = MutableLiveData<Pair<Boolean, String>>()
    val actionResult: LiveData<Pair<Boolean, String>> get() = _actionResult

    init {
        fetchUserSubscriptions()
    }

    fun fetchUserSubscriptions() {
        _isLoading.value = true
        viewModelScope.launch {
            val userUid = auth.currentUser?.uid
            if (userUid != null) {
                val subs = subscriptionRepository.getSubscriptionsByUserId(userUid)
                _subscriptions.value = subs
            } else {
                _subscriptions.value = emptyList()
                _actionResult.value = Pair(false, "User not logged in.")
            }
            _isLoading.value = false
        }
    }

    fun pauseSubscription(subscription: Subscription, pauseStartDate: Long, pauseEndDate: Long) {
        viewModelScope.launch {
            if (subscription.documentId.isEmpty()) {
                _actionResult.value = Pair(false, "Subscription ID is missing.")
                return@launch
            }

            val updates = mapOf(
                "status" to "PAUSED",
                "pause_periode_start" to Timestamp(Date(pauseStartDate)),
                "pause_periode_end" to Timestamp(Date(pauseEndDate))
            )
            val result = subscriptionRepository.updateSubscriptionStatus(subscription.documentId, updates)
            if (result) {
                _actionResult.value = Pair(true, "Subscription paused successfully!")
                fetchUserSubscriptions() // Refresh list
            } else {
                _actionResult.value = Pair(false, "Failed to pause subscription.")
            }
        }
    }

    fun resumeSubscription(subscription: Subscription) {
        viewModelScope.launch {
            if (subscription.documentId.isEmpty()) {
                _actionResult.value = Pair(false, "Subscription ID is missing.")
                return@launch
            }

            val updates = mapOf(
                "status" to "ACTIVE",
                "pause_periode_start" to FieldValue.delete(),
                "pause_periode_end" to FieldValue.delete()
            )
            val result = subscriptionRepository.updateSubscriptionStatus(subscription.documentId, updates)
            if (result) {
                _actionResult.value = Pair(true, "Subscription resumed successfully!")
                fetchUserSubscriptions()
            } else {
                _actionResult.value = Pair(false, "Failed to resume subscription.")
            }
        }
    }

    fun cancelSubscription(subscription: Subscription) {
        viewModelScope.launch {
            if (subscription.documentId.isEmpty()) {
                _actionResult.value = Pair(false, "Subscription ID is missing.")
                return@launch
            }

            val updates = mapOf(
                "status" to "CANCELED"
            )
            val result = subscriptionRepository.updateSubscriptionStatus(subscription.documentId, updates)
            if (result) {
                _actionResult.value = Pair(true, "Subscription canceled successfully!")
                fetchUserSubscriptions()
            } else {
                _actionResult.value = Pair(false, "Failed to cancel subscription.")
            }
        }
    }
}