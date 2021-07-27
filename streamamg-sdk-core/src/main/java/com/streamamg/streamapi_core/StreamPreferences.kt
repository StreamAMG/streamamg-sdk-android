package com.streamamg.streamapi_core

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.streamamg.streamapi_core.logging.logCR
import com.streamamg.streamapi_core.logging.logErrorCR

object StreamPreferences {
    private var preferences: SharedPreferences? = null

    fun initialisePreferences(context: Context){
        logCR("Preferences initialised")
        preferences = context.getSharedPreferences("stream_preferences", Context.MODE_PRIVATE)
    }

    fun isInitialised(): Boolean {
        return when (preferences == null){
            false -> true
            true -> {
                logInitialisationWarning()
                false
            }
        }
    }

    fun writePreference(key: String, value: Any){
        preferences?.let { preferences ->
            val edit = preferences.edit()
            when (value) {
                is String -> edit.putString(key, value)
                is Int -> edit.putInt(key, value)
                is Long -> edit.putLong(key, value)
                is Boolean -> edit.putBoolean(key, value)
                is Float -> edit.putFloat(key, value)
                is Double -> edit.putFloat(key, value.toFloat())
                else -> {
                    // Do Nothing
                }
            }
            edit.apply()
            return
        }
        logInitialisationWarning()
    }

    fun readString(key: String): String? {
        preferences?.let {preferences ->
            return preferences.getString(key, null)
        }
        logInitialisationWarning()
        return null
    }

    fun readString(key: String, default:String = ""): String {
        preferences?.let {preferences ->
        return preferences.getString(key, default) ?: default
        }
        logInitialisationWarning()
        return default
    }

    fun readInt(key: String): Int? {
        preferences?.let {preferences ->
        return preferences.getInt(key, 0)
        }
        logInitialisationWarning()
        return 0
    }

    fun readInt(key: String, default:Int = -1): Int {
        preferences?.let {preferences ->
        return preferences.getInt(key, default)
        }
        logInitialisationWarning()
        return default
    }

    fun readLong(key: String): Long? {
        preferences?.let {preferences ->
        return preferences.getLong(key, 0L)
        }
        logInitialisationWarning()
        return 0L
    }

    fun readLong(key: String, default:Long = 0L): Long {
        preferences?.let {preferences ->
        return preferences.getLong(key, default)
        }
        logInitialisationWarning()
        return default
    }

    fun readBoolean(key: String): Boolean? {
        preferences?.let {preferences ->
        return preferences.getBoolean(key, false)
        }
        logInitialisationWarning()
        return false
    }

    fun readBoolean(key: String, default:Boolean = false): Boolean {
        preferences?.let {preferences ->
        return preferences.getBoolean(key, default)
        }
        logInitialisationWarning()
        return default
    }

    fun readFloat(key: String): Float? {
        preferences?.let {preferences ->
        return preferences.getFloat(key, 0.0f)
        }
        logInitialisationWarning()
        return 0.0f
    }

    fun readFloat(key: String, default:Float = 0.0f): Float {
        preferences?.let {preferences ->
        return preferences.getFloat(key, default)
        }
        logInitialisationWarning()
        return default
    }

    fun readDouble(key: String): Double? {
        preferences?.let {preferences ->
        return preferences.getFloat(key, 0.0f).toDouble()
        }
        logInitialisationWarning()
        return 0.0
    }

    fun readDouble(key: String, value: Any, default:Double = 0.0): Double {
        preferences?.let {preferences ->
        return preferences.getFloat(key, default.toFloat()).toDouble()
        }
        logInitialisationWarning()
        return default
    }

    private fun logInitialisationWarning() {
        logErrorCR("StreamSDK Core is not initialised - run 'StreamAMGSDK.initialise(context)' to resolve this issue")
    }
}