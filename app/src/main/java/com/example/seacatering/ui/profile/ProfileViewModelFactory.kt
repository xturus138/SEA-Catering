package com.example.seacatering.ui.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seacatering.data.DataStoreManager

class ProfileViewModelFactory(
    private val dataStoreManager: DataStoreManager,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(dataStoreManager, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
