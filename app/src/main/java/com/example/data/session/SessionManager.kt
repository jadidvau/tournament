package com.example.data.session

import android.content.Context
import android.util.Log
import com.example.data.model.ORGANIZER_EMAIL
import com.example.data.model.UserProfile
import com.example.data.model.UserRole

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("efootball_auth_session", Context.MODE_PRIVATE)

    fun saveSession(user: UserProfile) {
        val isAdmin = user.email.trim().equals(ORGANIZER_EMAIL, ignoreCase = true)
        val role = if (isAdmin) UserRole.ADMIN else UserRole.PLAYER
        val userToSave = user.copy(role = role)

        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("uid", userToSave.uid)
            putString("full_name", userToSave.fullName)
            putString("email", userToSave.email)
            putString("phone_number", userToSave.phoneNumber)
            putString("in_game_id", userToSave.inGameId)
            putString("in_game_username", userToSave.inGameUsername)
            putString("role", userToSave.role.name)
            putBoolean("is_email_verified", userToSave.isEmailVerified)
            putLong("created_at", userToSave.createdAt)
            apply()
        }
        Log.d("SessionManager", "Saved auth session for: ${userToSave.email} (Role: ${userToSave.role})")
    }

    fun getSession(): UserProfile? {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (!isLoggedIn) return null

        val email = prefs.getString("email", "")?.trim() ?: ""
        if (email.isBlank()) return null

        val isAdmin = email.equals(ORGANIZER_EMAIL, ignoreCase = true)
        val role = if (isAdmin) UserRole.ADMIN else UserRole.PLAYER

        return UserProfile(
            uid = prefs.getString("uid", "user_${System.currentTimeMillis()}") ?: "",
            fullName = prefs.getString("full_name", "Player") ?: "Player",
            email = email,
            phoneNumber = prefs.getString("phone_number", "") ?: "",
            inGameId = prefs.getString("in_game_id", "") ?: "",
            inGameUsername = prefs.getString("in_game_username", "") ?: "",
            role = role,
            isEmailVerified = prefs.getBoolean("is_email_verified", true),
            createdAt = prefs.getLong("created_at", System.currentTimeMillis())
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        Log.d("SessionManager", "Cleared auth session")
    }
}
