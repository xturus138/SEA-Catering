package com.example.seacatering.data.repository

import com.example.seacatering.model.RecommendedItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HomeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val recommended = db.collection("recommended")
    private val adds = db.collection("adds")

    suspend fun getRecommendedItems(): List<RecommendedItem> {
        val itemList = mutableListOf<RecommendedItem>()
        try {
            val snapshot = recommended.get().await()
            for (document in snapshot.documents) {
                val title = document.getString("title") ?: ""
                val photoResId = document.getString("photoResId") ?: ""
                if (title.isNotEmpty() && photoResId.isNotEmpty()) {
                    itemList.add(RecommendedItem(title, photoResId))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return itemList
    }

    suspend fun getAddsItems(): List<String> {
        val itemList = mutableListOf<String>()
        try {
            val snapshot = adds.get().await()
            for (document in snapshot.documents) {
                val photoResId = document.getString("photoResId") ?: ""
                if (photoResId.isNotEmpty()) {
                    itemList.add(photoResId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return itemList
    }

}