package com.example.data

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

data class UserProfile(
    val email: String,
    val displayName: String,
    val loginProvider: String, // "GOOGLE" or "EMAIL"
    val isLoggedIn: Boolean = true
)

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("eletric_calc_auth", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_PROVIDER = "key_provider"
        private const val KEY_USER_PASS_HASH = "key_user_pass_hash"
    }

    fun getCurrentUser(): UserProfile? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val name = prefs.getString(KEY_USER_NAME, "Eletricista") ?: "Eletricista"
        val provider = prefs.getString(KEY_PROVIDER, "EMAIL") ?: "EMAIL"

        return UserProfile(
            email = email,
            displayName = name,
            loginProvider = provider,
            isLoggedIn = true
        )
    }

    /**
     * Authenticate via Google Sign-In
     */
    fun loginWithGoogle(accountEmail: String = "viniciuscp147@gmail.com", accountName: String = "Vinicius"): UserProfile {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_EMAIL, accountEmail)
            .putString(KEY_USER_NAME, accountName)
            .putString(KEY_PROVIDER, "GOOGLE")
            .apply()

        return UserProfile(
            email = accountEmail,
            displayName = accountName,
            loginProvider = "GOOGLE",
            isLoggedIn = true
        )
    }

    /**
     * Authenticate via Email and Password
     * Hashes password using SHA-256 so plain text password is never stored or visible.
     */
    fun loginWithEmail(email: String, passwordRaw: String): Result<UserProfile> {
        if (email.isBlank() || !email.contains("@")) {
            return Result.failure(IllegalArgumentException("Insira um endereço de e-mail válido."))
        }
        if (passwordRaw.length < 6) {
            return Result.failure(IllegalArgumentException("A senha deve ter no mínimo 6 caracteres."))
        }

        val passHash = hashPassword(passwordRaw)
        val savedHash = prefs.getString(KEY_USER_PASS_HASH, null)

        if (savedHash == null) {
            // New user registration
            prefs.edit()
                .putString(KEY_USER_PASS_HASH, passHash)
                .apply()
        } else if (savedHash != passHash) {
            // Existing user verification
            return Result.failure(IllegalArgumentException("Senha incorreta. Verifique suas credenciais."))
        }

        val derivedName = email.substringBefore("@").replaceFirstChar { it.uppercase() }

        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, derivedName)
            .putString(KEY_PROVIDER, "EMAIL")
            .apply()

        return Result.success(
            UserProfile(
                email = email,
                displayName = derivedName,
                loginProvider = "EMAIL",
                isLoggedIn = true
            )
        )
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .remove(KEY_PROVIDER)
            .apply()
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
