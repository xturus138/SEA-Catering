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

            true
        } catch (e: Exception) {

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

            true
        } catch (e: Exception) {

            false
        }
    }

    suspend fun deleteSubscription(documentId: String): Boolean {
        return try {
            subscriptionCollection.document(documentId).delete().await()

            true
        } catch (e: Exception) {

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

            0
        }
    }
}