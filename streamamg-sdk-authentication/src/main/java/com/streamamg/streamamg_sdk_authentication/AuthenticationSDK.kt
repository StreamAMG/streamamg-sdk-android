package com.streamamg.streamamg_sdk_authentication

import com.streamamg.streamamg_sdk_authentication.api.LoginResponse
import com.streamamg.streamamg_sdk_authentication.model.*
import com.streamamg.streamapi_core.StreamAMGSDK

interface AuthenticationSDK {
    val hasSavedCredentials: Boolean
    val lastLoginResponse: LoginResponse?
    fun isInitialized(): Boolean
    fun isLoggedIn(): Boolean
    fun initWithURL(url: String, params: String?)
    fun login(email: String, password: String, callback: (LoginResult) -> Unit)
    fun loginSilent(callback: (LoginResult) -> Unit)
    @Deprecated("Use login with callback which returns the token")
    fun loginSyncToken(email: String, password: String): String
    fun getKS(entryID: String, callback: (GetKeySessionResult) -> Unit)
    fun logout(callback: (LogoutResult) -> Unit)

    companion object {
        private val sdk: AuthenticationSDK by lazy { StreamAMGAuthenticationSDK(StreamAMGSDK.getInstance()) }
        fun getInstance(): AuthenticationSDK {
            return sdk
        }
    }
}