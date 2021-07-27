package com.streamamg.streamamg_sdk_authentication.model

sealed class GetKeySessionResult {
    data class Granted(val keySession: String?) : GetKeySessionResult()
    data class NoActiveSession(val msg: String?) : GetKeySessionResult()
    data class NoSubscription(val msg: String?) : GetKeySessionResult()
    data class NoEntitlement(val msg: String?) : GetKeySessionResult()
    data class Blocked(val msg: String?) : GetKeySessionResult()
    data class TooManyRequests(val msg: String?) : GetKeySessionResult()
    data class InvalidEntryId(val msg: String?) : GetKeySessionResult()
    data class Error(val throwable: Throwable?) : GetKeySessionResult()
}