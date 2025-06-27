package com.example.seacatering.ui.home

import android.Manifest
import android.app.Application
import android.location.Geocoder
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class HomeViewModel(app: Application): AndroidViewModel(app) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(app)

    private val _locationName = MutableLiveData<String>()
    val locationName = _locationName

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun fetchUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                getCityAndProvince(it)
            } ?: run {
                _locationName.postValue("Location not available")
            }
        }
    }

    private fun getCityAndProvince(location: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val city = addresses[0].locality ?: ""
                    val province = addresses[0].adminArea ?: ""
                    _locationName.postValue("$city, $province")
                } else {
                    _locationName.postValue("Unknown location")
                }
            } catch (e: Exception) {
                _locationName.postValue("Error: ${e.message}")
            }
        }
    }


}