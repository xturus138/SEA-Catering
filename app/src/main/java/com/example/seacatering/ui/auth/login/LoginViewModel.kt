package com.example.seacatering.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.seacatering.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel: ViewModel() {
    private val authRepository = AuthRepository()

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _googleLoginResult = MutableLiveData<Boolean>()
    val googleLoginResult: LiveData<Boolean> get() = _googleLoginResult

    fun login(email: String, password: String) {
        if(email.isEmpty() && password.isEmpty()){
            _errorMessage.value = "Fields cannot be empty"
            return
        }

        authRepository.loginUser(email, password).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                _loginResult.value = true
            } else {
                _errorMessage.value = task.exception?.message
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
                        email = user.email ?: "No Email"
                    )
                    _googleLoginResult.value = true
                } else {
                    _errorMessage.value = "User not found after Google sign-in"
                }
            } else {
                _errorMessage.value = task.exception?.message
            }
        }
    }
}