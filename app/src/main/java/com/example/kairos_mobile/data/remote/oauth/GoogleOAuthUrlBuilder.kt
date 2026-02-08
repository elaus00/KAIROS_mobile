package com.example.kairos_mobile.data.remote.oauth

import android.net.Uri

object GoogleOAuthUrlBuilder {
    const val REDIRECT_URI = "com.kairos.app:/oauth2redirect"
    const val REDIRECT_SCHEME = "com.kairos.app"
    const val REDIRECT_PATH = "/oauth2redirect"
    const val REDIRECT_HOST = "oauth2redirect"

    private const val AUTH_BASE_SCHEME = "https"
    private const val AUTH_BASE_AUTHORITY = "accounts.google.com"
    private const val SCOPE_CALENDAR_EVENTS = "https://www.googleapis.com/auth/calendar.events"

    fun buildAuthorizationUrl(clientId: String, state: String? = null): String? {
        if (clientId.isBlank()) return null

        return Uri.Builder()
            .scheme(AUTH_BASE_SCHEME)
            .authority(AUTH_BASE_AUTHORITY)
            .appendPath("o")
            .appendPath("oauth2")
            .appendPath("v2")
            .appendPath("auth")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("scope", SCOPE_CALENDAR_EVENTS)
            .appendQueryParameter("access_type", "offline")
            .appendQueryParameter("prompt", "consent")
            .apply {
                if (!state.isNullOrBlank()) {
                    appendQueryParameter("state", state)
                }
            }
            .build()
            .toString()
    }
}
