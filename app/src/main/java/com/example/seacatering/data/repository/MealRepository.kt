package com.example.seacatering.data.repository

import com.example.seacatering.model.Meals
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MealRepository {

    private val db = FirebaseFirestore.getInstance()
    private val mealCollection = db.collection("meals")

    suspend fun getMeals(): List<Meals> {
        return try {
            val snapshot = mealCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Meals::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
