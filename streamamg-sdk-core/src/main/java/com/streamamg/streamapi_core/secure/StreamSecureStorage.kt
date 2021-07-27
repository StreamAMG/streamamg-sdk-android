package com.streamamg.streamapi_core.secure

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

internal class StreamSecureStorage(context: Context) : SecureStorage {

    private val mainKey = MasterKey.Builder(context.applicationContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context.applicationContext,
        FILE_NAME,
        mainKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun save(key: String, value: String) {
        encryptedPreferences.edit().putString(key, value).apply()
    }

    override fun load(key: String): String? {
        return encryptedPreferences.getString(key, null)
    }

    override fun clear(key: String) {
        encryptedPreferences.edit().remove(key).apply()
    }

    companion object {
        const val FILE_NAME = "secure"
    }
}