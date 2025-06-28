package com.example.seacatering.data.repository

import android.util.Log
import com.example.seacatering.model.Subscription
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

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

    suspend fun getSubscriptionsByUserId(userId: String): List<Subscription> {
        return try {
            val snapshot = subscriptionCollection
                .whereEqualTo("user_uid", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Subscription::class.java)?.copy(documentId = doc.id)
            }
        } catch (e: Exception) {
            Log.e("SubscriptionRepository", "Error fetching subscription data: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateSubscriptionStatus(documentId: String, updates: Map<String, Any>): Boolean {
        return try {
            val finalUpdates = updates.toMutableMap()
            if (finalUpdates["status"] == "CANCELED") {
                finalUpdates["canceled_at"] = Timestamp.now()
            } else if (finalUpdates["status"] == "ACTIVE") {

            }

            subscriptionCollection.document(documentId).update(finalUpdates).await()
            Log.d("SubscriptionRepository", "Subscription $documentId updated successfully with status: ${finalUpdates["status"]}")
            true
        } catch (e: Exception) {
            Log.e("SubscriptionRepository", "Failed to update subscription $documentId: ${e.message}")
            false
        }
    }

    suspend fun deleteSubscription(documentId: String): Boolean {
        return try {
            subscriptionCollection.document(documentId).delete().await()
            Log.d("SubscriptionRepository", "Subscription $documentId deleted successfully")
            true
        } catch (e: Exception) {
            Log.e("SubscriptionRepository", "Failed to delete subscription $documentId: ${e.message}")
            false
        }
    }


    suspend fun getCanceledSubscriptionsInDateRange(startDate: Timestamp, endDate: Timestamp): Int {
        return try {
            val snapshot = subscriptionCollection
                .whereEqualTo("status", "CANCELED")
                .whereGreaterThanOrEqualTo("canceled_at", startDate)
                .whereLessThanOrEqualTo("canceled_at", endDate)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e("AdminDashboardRepo", "Error fetching canceled subscriptions in date range: ${e.message}")
            0
        }
    }
}