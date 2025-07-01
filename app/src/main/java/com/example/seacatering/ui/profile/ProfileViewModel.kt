package com.example.seacatering.ui.profile

import android.app.Application
import android.location.Geocoder
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.data.repository.AuthRepository
import com.example.seacatering.model.Role
import com.example.seacatering.model.Users
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ProfileViewModel(private val dataStoreManager: DataStoreManager, application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()
    private val geocoder = Geocoder(application, Locale.getDefault())

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> get() = _updateResult

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> get() = _errorMsg

    private val _tempProfileImageUri = MutableLiveData<Uri?>()
    val tempProfileImageUri: LiveData<Uri?> get() = _tempProfileImageUri

    private val _locationLatLng = MutableLiveData<Pair<Double, Double>>()
    val locationLatLng: LiveData<Pair<Double, Double>> get() = _locationLatLng

    private val _locationTitle = MutableLiveData<String>()
    val locationTitle: LiveData<String> get() = _locationTitle

    fun logout(){
        repository.logoutUser()
        viewModelScope.launch {
            dataStoreManager.clearUserData()
        }
    }

    fun updateUser(
        name: String,
        email: String,
        address: String,
        noHp: String,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            val currentUser = dataStoreManager.userData.first() ?: return@launch

            val updatedUser = currentUser.copy(
                name = name,
                email = email,
                address = address,
                noHp = noHp,
                latitude = latitude,
                longitude = longitude
            )

            dataStoreManager.saveUserData(updatedUser)
            _updateResult.postValue(true)
        }
    }


    fun setTempProfileImageUri(uri: Uri?) {
        _tempProfileImageUri.value = uri
    }

    fun setLocationLatLng(latLng: Pair<Double, Double>) {
        _locationLatLng.value = latLng
    }

    fun getCityAndProvince(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addressLine = addresses[0].getAddressLine(0)
                    _locationTitle.postValue(addressLine)
                } else {
                    _locationTitle.postValue("Unknown location")
                }
            } catch (e: Exception) {
                _locationTitle.postValue("Error: ${e.message}")
            }
        }
    }

    fun geocodeAddress(address: String, onResult: (Pair<Double, Double>?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                val results = geocoder.getFromLocationName(address, 1)
                val latLng = if (!results.isNullOrEmpty()) {
                    Pair(results[0].latitude, results[0].longitude)
                } else null
                withContext(Dispatchers.Main) {
                    onResult(latLng)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

}