package com.example.seacatering.data.repository

import android.util.Log
import com.example.seacatering.model.Meals
import com.example.seacatering.model.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MealRepository() {

    private val db = FirebaseFirestore.getInstance()
    private val mealCollection = db.collection("meals")

    suspend fun getMeals(): List<Meals> {
        val mealsWithMenu = mutableListOf<Meals>()
        try {
            val snapshot = mealCollection.get().await()
            for (document in snapshot.documents) {
                val id = document.id
                val name = document.getString("name") ?: ""
                val price = document.getString("price") ?: ""
                val descriptionTitle = document.getString("descriptionTitle") ?: ""
                val details = document.get("details") as? List<String> ?: emptyList()
                val imageResId = document.getString("imageResId") ?: ""

                val menuListSnapshot = mealCollection.document(id)
                    .collection("menuList")
                    .get()
                    .await()

                val menuList = menuListSnapshot.documents.mapNotNull { menuDoc ->
                    menuDoc.toObject(MenuItem::class.java)
                }

                mealsWithMenu.add(
                    Meals(
                        id = id,
                        name = name,
                        price = price,
                        descriptionTitle = descriptionTitle,
                        details = details,
                        imageResId = imageResId,
                        menuList = menuList
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mealsWithMenu
    }





}
