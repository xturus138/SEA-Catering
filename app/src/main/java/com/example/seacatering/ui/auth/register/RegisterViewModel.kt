package com.example.seacatering.ui.auth.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.seacatering.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth

class RegisterViewModel: ViewModel() {
    private val authRepository = AuthRepository()

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> get() = _registerResult

    private val _errorResult = MutableLiveData<String>()
    val errorResult: LiveData<String> get() = _errorResult

    private val _googleLoginResult = MutableLiveData<Boolean>()
    val googleLoginResult: LiveData<Boolean> get() = _googleLoginResult

    fun register(email: String, password: String, confirmPass: String, name: String, address: String, noHp: String){
        if (email.isEmpty() && password.isEmpty() && confirmPass.isEmpty() && name.isEmpty()){
            _errorResult.value = "Fields cannot be empty"
            return
        } else if (password != confirmPass) {
            _errorResult.value = "Password is not match"
            return
        }

        authRepository.registerUser(email, password).addOnCompleteListener{task ->
            if(task.isSuccessful){
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null){
                    Log.d("RegisterViewModel", "register: Get into saveUserData")
                    authRepository.saveUserData(uid, name, email, address, noHp)
                }
                _registerResult.value = true
            } else {
                _errorResult.value = task.exception?.message
            }
        }
    }

    fun loginWithGoogle(idToken: String){
        authRepository.loginWithGoogle(idToken).addOnCompleteListener { task ->
            if(task.isSuccessful){
                val user = FirebaseAuth.getInstance().currentUser
                if(user != null){
                    authRepository.saveUserData(
                        uid = user.uid,
                        name = user.displayName ?: "No Name",
                        email = user.email ?: "No Email",
                        address = "Not Provided",
                        noHp = "Not Provided"
                    )
                    _googleLoginResult.value = true
                } else {
                    _errorResult.value = "User not found after Google sign-in"
                }
            } else {
                _errorResult.value = task.exception?.message
            }
        }
    }
}