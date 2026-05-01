package com.company.product.mobile.data.local

import android.content.Context

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun token(): String? = prefs.getString("token", null)

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    fun clear() {
        prefs.edit().remove("token").apply()
    }
}
