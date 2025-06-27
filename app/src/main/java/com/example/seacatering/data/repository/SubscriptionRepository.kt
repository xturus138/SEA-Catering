package com.example.seacatering.data.repository

import android.util.Log
import com.example.seacatering.model.Subscription
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SubscriptionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val subscriptionCollection = db.collection("subscription")

    suspend fun saveSubscription(subscription: Subscription): Boolean {
        return try {
            subscriptionCollection.add(subscription).await()
            Log.d("SubscriptionRepository", "Subscription data saved successfully")
            true
        } catch (e: Exception) {
            Log.e("SubscriptionRepository", "Failed to save subscription data: ${e.message}")
            false
        }
    }

    suspend fun getSubscriptionByUserId(userId: String): List<Subscription> {
        return try {
            val snapshot = subscriptionCollection
                .whereEqualTo("user_uid", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Subscription::class.java) }
        } catch (e: Exception) {
            Log.e("SubscriptionRepository", "Error fetching subscription data: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateSubscription(documentId: String, updatedSubscription: Map<String, Any>): Boolean {
        return try {
            subscriptionCollection.document(documentId).update(updatedSubscription).await()
            Log.d("SubscriptionRepository", "Subscription data updated successfully")
            true
        } catch (e: Exception) {
            Log.e("SubscriptionRepository", "Failed to update subscription data: ${e.message}")
            false
        }
    }
}