package com.streamamg.streamapi_core.secure

interface SecureStorage {
    fun save(key: String, value: String)
    fun load(key: String): String?
    fun clear(key: String)
}