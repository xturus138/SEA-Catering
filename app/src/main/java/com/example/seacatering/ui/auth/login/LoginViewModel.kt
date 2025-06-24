package com.example.seacatering.ui.auth.login

import android.app.Application
import androidx.datastore.core.DataStore
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LoginViewModel (private val app: Application) : AndroidViewModel(app) {
    private val authRepository = AuthRepository()
    private val dataStoreManager = DataStoreManager(app.applicationContext)

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
                val user = FirebaseAuth.getInstance().currentUser
                user?.let {
                    viewModelScope.launch {
                        dataStoreManager.saveUserData(
                            Users(
                                name = it.displayName ?: "No Name",
                                email = it.email ?: "No Email",
                                address = "Not Provided",
                                noHp = "Not Provided",
                                role = Role.USER
                            )
                        )
                    }
                }
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
                        email = user.email ?: "No Email",
                        address = "Not Provided",
                        noHp = "Not Provided"
                    )
                    viewModelScope.launch {
                        dataStoreManager.saveUserData(
                            Users(
                                name = user.displayName ?: "No Name",
                                email = user.email ?: "No Email",
                                address = "Not Provided",
                                noHp = "Not Provided",
                                role = Role.USER
                            )
                        )
                    }
                    _googleLoginResult.value = true
                } else {
                    _errorMessage.value = "User not found after Google sign-in"
                }
            } else {
                _errorMessage.value = task.exception?.message
            }
        }
    }

    fun isUserLoggedIn(): Flow<Boolean> = dataStoreManager.userData.map { it != null }

}