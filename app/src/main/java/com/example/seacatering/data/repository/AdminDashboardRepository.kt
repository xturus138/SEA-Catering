package com.example.seacatering.data.repository

import android.util.Log
import com.example.seacatering.model.Subscription
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit

class AdminDashboardRepository {

    private val db = FirebaseFirestore.getInstance()
    private val subscriptionCollection = db.collection("subscription")
    private val usersCollection = db.collection("users")



    suspend fun getAllSubscriptions(): List<Subscription> {
        return try {
            val snapshot = subscriptionCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Subscription::class.java)?.copy(documentId = doc.id)
            }
        } catch (e: Exception) {
            Log.e("AdminDashboardRepo", "Error fetching all subscriptions: ${e.message}")
            emptyList()
        }
    }

    suspend fun getActiveSubscriptionsCount(): Int {
        return try {
            val snapshot = subscriptionCollection
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e("AdminDashboardRepo", "Error fetching active subscriptions count: ${e.message}")
            0
        }
    }

    suspend fun getNewSubscriptionsInDateRange(startDate: Timestamp, endDate: Timestamp): Int {
        return try {
            val snapshot = subscriptionCollection
                .whereGreaterThanOrEqualTo("created_at", startDate)
                .whereLessThanOrEqualTo("created_at", endDate)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e("AdminDashboardRepo", "Error fetching new subscriptions in date range: ${e.message}")
            0
        }
    }


    suspend fun getCanceledSubscriptionsInDateRange(startDate: Timestamp, endDate: Timestamp): Int {
        return try {
            val snapshot = subscriptionCollection
                .whereEqualTo("status", "CANCELED")
                .whereGreaterThanOrEqualTo("end_date", startDate)
                .whereLessThanOrEqualTo("end_date", endDate)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e("AdminDashboardRepo", "Error fetching canceled subscriptions in date range: ${e.message}")
            0
        }
    }
}