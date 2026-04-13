package com.example.wnhu_android_app

import android.content.Context

object SessionStore {
    private const val PREFS_NAME = "wnhu_auth_state"
    private const val KEY_LAST_EMAIL = "last_email"
    private const val KEY_PROFILE_COMPLETE = "profile_complete"

    fun saveSignedInUser(context: Context, email: String, profileComplete: Boolean) {
        prefs(context).edit()
            .putString(KEY_LAST_EMAIL, email)
            .putBoolean(KEY_PROFILE_COMPLETE, profileComplete)
            .apply()
    }

    fun getLastSignedInEmail(context: Context): String {
        return prefs(context).getString(KEY_LAST_EMAIL, "").orEmpty()
    }

    fun isProfileComplete(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_PROFILE_COMPLETE, false)
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
