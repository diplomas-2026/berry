package com.company.product.mobile.presentation

import android.content.Context
import com.company.product.mobile.data.local.SessionStore
import com.company.product.mobile.data.remote.AuthResponse
import com.company.product.mobile.data.remote.NetworkModule
import com.company.product.mobile.data.repository.AppRepository

data class SessionUi(
    val userId: Long,
    val email: String,
    val fullName: String,
    val role: String,
    val avatarUrl: String?
)

class AppState(context: Context) {
    private val sessionStore = SessionStore(context.applicationContext)
    private val repo = AppRepository(NetworkModule.api(context.applicationContext), sessionStore)

    var session: SessionUi? = null
        private set

    suspend fun login(email: String, password: String) {
        val auth = repo.login(email, password)
        session = auth.toSessionUi()
    }

    fun setSessionFromAuth(auth: AuthResponse) {
        session = auth.toSessionUi()
    }

    fun logout() {
        repo.logout()
        session = null
    }

    fun repository() = repo
}

private fun AuthResponse.toSessionUi() = SessionUi(
    userId = userId,
    email = email,
    fullName = fullName,
    role = role,
    avatarUrl = avatarUrl
)
