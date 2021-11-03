package com.streamamg.streamapi_core.secure

import android.content.Context
import android.content.Context.MODE_PRIVATE
//import android.content.SharedPreferences
//import androidx.security.crypto.EncryptedSharedPreferences
//import androidx.security.crypto.MasterKey

internal class StreamSecureStorage(context: Context) : SecureStorage {

//    private val mainKey = MasterKey.Builder(context.applicationContext)
//        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
//        .build()
//
//    private val encryptedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
//        context.applicationContext,
//        FILE_NAME,
//        mainKey,
//        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//    )

    private val sharedPreferences = context.getSharedPreferences("${context.packageName}_prefs", MODE_PRIVATE)

    override fun save(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun load(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun clear(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

//    companion object {
//        const val FILE_NAME = "secure"
//    }
}