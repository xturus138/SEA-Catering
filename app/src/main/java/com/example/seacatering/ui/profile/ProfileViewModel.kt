package com.example.seacatering.ui.profile

import android.net.Uri // Import Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.data.repository.AuthRepository
import com.example.seacatering.model.Role
import com.example.seacatering.model.Users
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProfileViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    private val repository = AuthRepository()

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> get() = _updateResult

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> get() = _errorMsg

    private val _tempProfileImageUri = MutableLiveData<Uri?>()
    val tempProfileImageUri: LiveData<Uri?> get() = _tempProfileImageUri

    fun logout(){
        repository.logoutUser()
        viewModelScope.launch {
            dataStoreManager.clearUserData()
        }
    }

    fun updateUser(name: String, email: String, address: String, noHp: String){
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        repository.updateUser(uid, name, email,address, noHp)
            .addOnSuccessListener {
                viewModelScope.launch {
                    val currentUserData = dataStoreManager.userData.firstOrNull()
                    val updatedUser = currentUserData?.copy(
                        name = name,
                        email = email,
                        address = address,
                        noHp = noHp,
                        profileImageUrl = tempProfileImageUri.value.toString()
                    ) ?: Users(
                        uid = uid,
                        name = name,
                        email = email,
                        address = address,
                        noHp = noHp,
                        role = Role.USER,
                        profileImageUrl = tempProfileImageUri.value.toString()
                    )
                    dataStoreManager.saveUserData(updatedUser)
                    _updateResult.value = true
                }

            }
            .addOnFailureListener {
                _errorMsg.value = it.message
            }
    }

    fun setTempProfileImageUri(uri: Uri?) {
        _tempProfileImageUri.value = uri
    }
}