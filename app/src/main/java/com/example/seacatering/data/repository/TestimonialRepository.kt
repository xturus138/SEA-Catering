package com.example.seacatering.data.repository

import android.util.Log
import com.example.seacatering.model.Testimonial
import com.example.seacatering.model.Users
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TestimonialRepository {

    private val db = FirebaseFirestore.getInstance()
    private val testimonialCollection = db.collection("testimonial")
    private val userCollection = db.collection("users")

    suspend fun saveTestimonial(testimonial: Testimonial): Boolean {
        return try {
            testimonialCollection.add(testimonial).await()
            Log.d("TestimonialRepository", "Testimonial data saved successfully")
            true
        } catch (e: Exception) {
            Log.e("TestimonialRepository", "Failed to save testimonial data: ${e.message}")
            false
        }
    }

    suspend fun getAllTestimonials(): List<Testimonial> {
        return try {
            val snapshot = testimonialCollection.get().await()
            val testimonials = mutableListOf<Testimonial>()

            for (document in snapshot.documents) {
                val testimonial = document.toObject(Testimonial::class.java)
                testimonial?.let {
                    val userDoc = userCollection.document(it.user_uid).get().await()
                    val user = userDoc.toObject(Users::class.java)
                    val customerName = user?.name ?: "Anonymous"

                    testimonials.add(it.copy(customerName = customerName))
                }
            }
            testimonials
        } catch (e: Exception) {
            Log.e("TestimonialRepository", "Error fetching testimonials: ${e.message}")
            emptyList()
        }
    }
}