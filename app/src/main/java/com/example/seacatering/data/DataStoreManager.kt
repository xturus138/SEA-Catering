package com.example.seacatering.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.seacatering.model.Role
import com.example.seacatering.model.Users
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name ="user_preferences")

class DataStoreManager(private val context: Context) {


    private object PreferencesKeys {
        val UID = stringPreferencesKey("uid")
        val NAMA = stringPreferencesKey("nama")
        val EMAIL = stringPreferencesKey("email")
        val PHONE = stringPreferencesKey("phone")
        val ADDRESS = stringPreferencesKey("address")
        val ROLE = stringPreferencesKey("role")
        val PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
    }

    suspend fun saveUserData(user: Users) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UID] = user.uid
            preferences[PreferencesKeys.NAMA] = user.name
            preferences[PreferencesKeys.EMAIL] = user.email
            preferences[PreferencesKeys.PHONE] = user.noHp
            preferences[PreferencesKeys.ADDRESS] = user.address
            preferences[PreferencesKeys.ROLE] = user.role.name
            preferences[PreferencesKeys.PROFILE_IMAGE_URL] = user.profileImageUrl
        }
    }


    suspend fun clearUserData(){
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    val userData: Flow<Users?> = context.dataStore.data.map { preferences ->
        val uid = preferences[PreferencesKeys.UID]
        val name = preferences[PreferencesKeys.NAMA]
        val email = preferences[PreferencesKeys.EMAIL]
        val phone = preferences[PreferencesKeys.PHONE]
        val address = preferences[PreferencesKeys.ADDRESS]
        val roleString = preferences[PreferencesKeys.ROLE]
        val profileImageUrl = preferences[PreferencesKeys.PROFILE_IMAGE_URL]

        if (uid != null && name != null && email != null && phone != null && address != null && roleString != null) {
            val role = try {
                Role.valueOf(roleString)
            } catch (e: IllegalArgumentException) {
                Role.USER
            }

            Users(uid, name, email, address, phone, role, profileImageUrl ?: "")
        } else {
            null
        }
    }
}