package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class UserAccount(
    val email: String,
    val fullName: String,
    val securityQuestion: String
)

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("keuanganku_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_USER_EMAIL = "logged_in_user_email"
    }

    /**
     * Registers a new user. Returns false if email is already taken.
     */
    fun registerUser(fullName: String, email: String, password: String, question: String, answer: String): Boolean {
        val cleanedEmail = email.trim().lowercase()
        if (cleanedEmail.isEmpty() || password.isEmpty()) return false
        
        // Check if user already exists
        if (prefs.contains("user_pwd_$cleanedEmail")) {
            return false
        }

        // Store user details in separate keys for simplicity and offline robustness
        prefs.edit()
            .putString("user_fullname_$cleanedEmail", fullName.trim())
            .putString("user_pwd_$cleanedEmail", password)
            .putString("user_question_$cleanedEmail", question.trim())
            .putString("user_answer_$cleanedEmail", answer.trim().lowercase())
            .apply()

        return true
    }

    /**
     * Validates credentials and logins. Returns the UserAccount if successful.
     */
    fun login(email: String, password: String): UserAccount? {
        val cleanedEmail = email.trim().lowercase()
        if (cleanedEmail.isEmpty()) return null

        val storedPassword = prefs.getString("user_pwd_$cleanedEmail", null)
        if (storedPassword != null && storedPassword == password) {
            val fullName = prefs.getString("user_fullname_$cleanedEmail", "Pengguna") ?: "Pengguna"
            val question = prefs.getString("user_question_$cleanedEmail", "") ?: ""
            
            // Set session
            prefs.edit().putString(KEY_CURRENT_USER_EMAIL, cleanedEmail).apply()
            return UserAccount(cleanedEmail, fullName, question)
        }
        return null
    }

    /**
     * Returns the security question for a registered email.
     */
    fun getSecurityQuestion(email: String): String? {
        val cleanedEmail = email.trim().lowercase()
        if (!prefs.contains("user_pwd_$cleanedEmail")) {
            return null
        }
        return prefs.getString("user_question_$cleanedEmail", null)
    }

    /**
     * Checks if safety question matching answer is correct, then updates the password.
     */
    fun resetPassword(email: String, answer: String, newPassword: String): Boolean {
        val cleanedEmail = email.trim().lowercase()
        if (!prefs.contains("user_pwd_$cleanedEmail")) return false

        val storedAnswer = prefs.getString("user_answer_$cleanedEmail", null)
        if (storedAnswer != null && storedAnswer.equals(answer.trim(), ignoreCase = true)) {
            prefs.edit().putString("user_pwd_$cleanedEmail", newPassword).apply()
            return true
        }
        return false
    }

    /**
     * Returns true if user is currently logged in.
     */
    fun isLoggedIn(): Boolean {
        return getCurrentUserEmail() != null
    }

    /**
     * Returns current logged in user's email.
     */
    fun getCurrentUserEmail(): String? {
        return prefs.getString(KEY_CURRENT_USER_EMAIL, null)
    }

    /**
     * Returns full name of current user or specific user.
     */
    fun getUserFullName(email: String? = getCurrentUserEmail()): String {
        if (email == null) return "Guest User"
        return prefs.getString("user_fullname_${email.trim().lowercase()}", "Pengguna") ?: "Pengguna"
    }

    /**
     * Clears current session, logging out the user.
     */
    fun logout() {
        prefs.edit().remove(KEY_CURRENT_USER_EMAIL).apply()
    }
}
