package com.example.seacatering.ui.dashboard.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.data.repository.AdminDashboardRepository
import com.example.seacatering.data.repository.AuthRepository
import com.example.seacatering.model.Subscription
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class AdminDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AdminDashboardRepository()
    private val authRepository = AuthRepository()
    private val dataStoreManager = DataStoreManager(application.applicationContext)

    private val _newSubscriptions = MutableLiveData<Int>()
    val newSubscriptions: LiveData<Int> get() = _newSubscriptions

    private val _monthlyRecurringRevenue = MutableLiveData<Double>()
    val monthlyRecurringRevenue: LiveData<Double> get() = _monthlyRecurringRevenue

    private val _canceledSubscriptionsCount = MutableLiveData<Int>()
    val canceledSubscriptionsCount: LiveData<Int> get() = _canceledSubscriptionsCount

    private val _subscriptionGrowth = MutableLiveData<Int>()
    val subscriptionGrowth: LiveData<Int> get() = _subscriptionGrowth

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> get() = _errorMessage

    private val _logoutResult = MutableLiveData<Boolean>()
    val logoutResult: LiveData<Boolean> get() = _logoutResult

    private var currentStartDate: Timestamp
    private var currentEndDate: Timestamp

    init {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        currentStartDate = Timestamp(calendar.time)

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        currentEndDate = Timestamp(calendar.time)

        fetchDashboardData(currentStartDate, currentEndDate)
    }

    fun fetchDashboardData(startDate: Timestamp, endDate: Timestamp) {
        _isLoading.value = true
        _errorMessage.value = null
        currentStartDate = startDate
        currentEndDate = endDate

        viewModelScope.launch {
            try {

                val allSubscriptions = repository.getAllSubscriptions()


                _newSubscriptions.value = allSubscriptions.filter {
                    it.created_at != null &&
                            it.created_at!!.toDate().after(startDate.toDate()) &&
                            it.created_at!!.toDate().before(endDate.toDate())
                }.size

                _canceledSubscriptionsCount.value = allSubscriptions.filter { sub ->
                    sub.status == "CANCELED" &&
                            sub.canceled_at != null &&
                            sub.canceled_at!!.toDate().after(startDate.toDate()) &&
                            sub.canceled_at!!.toDate().before(endDate.toDate())
                }.size

                val mrrSubscriptions = allSubscriptions.filter { sub ->
                    sub.status == "ACTIVE" &&
                            (sub.created_at != null && sub.created_at!!.toDate().before(endDate.toDate())) &&
                            (sub.end_date == null || sub.end_date!!.toDate().after(startDate.toDate())) &&
                            (sub.pause_periode_start == null || sub.pause_periode_end == null ||
                                    !(sub.pause_periode_start!!.toDate().before(endDate.toDate()) &&
                                            sub.pause_periode_end!!.toDate().after(startDate.toDate())))
                }
                _monthlyRecurringRevenue.value = mrrSubscriptions.sumOf { it.total_price }


                val activeAtEndDate = allSubscriptions.filter { sub ->
                    sub.status == "ACTIVE" &&
                            (sub.created_at != null && sub.created_at!!.toDate().before(endDate.toDate())) &&
                            (sub.end_date == null || sub.end_date!!.toDate().after(endDate.toDate())) &&
                            (sub.pause_periode_start == null || sub.pause_periode_end == null ||
                                    sub.pause_periode_end!!.toDate().before(endDate.toDate()) ||
                                    sub.pause_periode_start!!.toDate().after(endDate.toDate()))
                }.size
                _subscriptionGrowth.value = activeAtEndDate

            } catch (e: Exception) {
                _errorMessage.value = "Failed to load dashboard data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logoutUser()
                dataStoreManager.clearUserData()
                _logoutResult.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Logout failed: ${e.message}"
                _logoutResult.value = false
            }
        }
    }
}