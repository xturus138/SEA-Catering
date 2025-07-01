package com.example.seacatering.ui.auth.login

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.seacatering.data.DataStoreManager
import com.example.seacatering.data.repository.AuthRepository
import com.example.seacatering.model.Role
import com.example.seacatering.model.Users
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginViewModel(private val app: Application) : AndroidViewModel(app) {
    private val authRepository = AuthRepository()
    private val dataStoreManager = DataStoreManager(app.applicationContext)

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _googleLoginResult = MutableLiveData<Boolean>()
    val googleLoginResult: LiveData<Boolean> get() = _googleLoginResult

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Fields cannot be empty"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Invalid email format"
            return
        }

        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }

        authRepository.loginUser(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    viewModelScope.launch {
                        val user = authRepository.getUserData(uid)
                        if (user != null) {
                            dataStoreManager.saveUserData(user)
                            _loginResult.value = true
                        } else {
                            _errorMessage.value = "User data not found"
                        }
                    }
                } else {
                    _errorMessage.value = "User ID not found"
                }
            } else {
                _errorMessage.value = "Login failed. Please check your credentials and try again."
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            _errorMessage.value = "Google ID token is missing"
            return
        }

        authRepository.loginWithGoogle(idToken).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    viewModelScope.launch {
                        val existingUser = authRepository.getUserData(user.uid)
                        if (existingUser != null) {
                            dataStoreManager.saveUserData(existingUser)
                        } else {
                            val newUser = Users(
                                uid = user.uid,
                                name = user.displayName ?: "No Name",
                                email = user.email ?: "No Email",
                                address = "Not Provided",
                                noHp = "Not Provided",
                                role = Role.USER
                            )
                            authRepository.saveUserData(
                                user.uid,
                                newUser.name,
                                newUser.email,
                                newUser.address,
                                newUser.noHp
                            )
                            dataStoreManager.saveUserData(newUser)
                        }
                        _googleLoginResult.value = true
                    }
                } else {
                    _errorMessage.value = "User not found after Google sign-in"
                }
            } else {
                _errorMessage.value = "Unknown Error. Please contact admin."
            }
        }
    }
}
