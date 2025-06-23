package com.example.seacatering.data.repository

import android.util.Log
import com.example.seacatering.model.Role
import com.example.seacatering.model.Users
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

class AuthRepository() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()


    fun loginUser(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(email, password)
    }

    fun loginWithGoogle(idToken: String): Task<AuthResult> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return FirebaseAuth.getInstance().signInWithCredential(credential)
    }

    fun registerUser(email: String, password: String): Task<AuthResult> {
        return  firebaseAuth.createUserWithEmailAndPassword(email, password)
    }

    fun saveUserData(uid: String, name: String, email: String, address: String, noHp: String){
        val user = Users(
            name = name,
            email = email,
            address = address,
            noHp = noHp,
            role = Role.USER
        )
        db.collection("users").document(uid).set(user).addOnCompleteListener{
            Log.d("Firestore", "saveUserData: User data saved successfully")
        }
            .addOnFailureListener{
                Log.e("Firestore", "Failed to save user data")
            }

    }


}