package com.flit.app.data.remote

import android.content.SharedPreferences
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DeviceIdProvider @Inject constructor(
    @Named("encrypted_prefs") private val prefs: SharedPreferences
) {
    companion object {
        private const val KEY_DEVICE_ID = "device_id"
    }

    fun getOrCreateDeviceId(): String {
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (!existing.isNullOrBlank()) {
            return existing
        }

        val generated = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, generated).apply()
        return generated
    }
}
