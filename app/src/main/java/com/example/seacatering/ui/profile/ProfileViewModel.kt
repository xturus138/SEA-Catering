package com.example.seacatering.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.data.repository.AuthRepository
import com.example.seacatering.model.Role
import com.example.seacatering.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.launch

class ProfileViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    private val repository = AuthRepository()

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> get() = _updateResult

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> get() = _errorMsg

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
                    val updatedUser = Users(name, email, address, noHp, Role.USER)
                    dataStoreManager.saveUserData(updatedUser)
                    _updateResult.value = true
                }

            }
            .addOnFailureListener {
                _errorMsg.value = it.message
            }
    }
}
