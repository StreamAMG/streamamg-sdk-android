package com.streamamg.streamamg_sdk_authentication.model

sealed class LogoutResult {
    data class LogoutOK(val user: StreamAMGUser) : LogoutResult()
    data class LogoutFailed(val throwable: Throwable) : LogoutResult()
}