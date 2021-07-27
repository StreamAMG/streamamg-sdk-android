package com.streamamg.streamamg_sdk_authentication.model

sealed class LoginResult {
    data class LoginOK(val user: StreamAMGUser) : LoginResult()
    data class WrongEmailOrPassword(val msg: String?) : LoginResult()
    object NoSavedCredentials : LoginResult()
    data class UserRestriction(val msg: String?) : LoginResult()
    data class Concurrency(val msg: String?) : LoginResult()
    data class Error(val error: Throwable?) : LoginResult()
}