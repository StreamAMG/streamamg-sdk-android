package com.streamamg.streamamg_sdk_authentication.api

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("Status")
    var status: Int? = null,

    @SerializedName("KSession")
    var kSession: String? = null,

    @SerializedName("ErrorMessage")
    var errorMessage: String? = null,

    @SerializedName("AuthenticationToken")
    var authenticationToken: String? = null,

    @SerializedName("UtcNow")
    var utcNow: Long? = null,

    @SerializedName("LocationFromIp")
    var locationFromIp: LocationFromIp? = null,

    @SerializedName("CurrentCustomerSessionStatus")
    var currentCustomerSessionStatus: Int? = null,

    @SerializedName("CurrentCustomerSession")
    var currentCustomerSession: CurrentCustomerSession? = null,

    @SerializedName("ModelErrors")
    var modelErrors: ModelErrors? = null
) {
    val hasErrors
        get() = modelErrors?.let {
            it.emailaddress != null || it.concurrency != null || it.password != null || it.restriction != null
        } ?: false
}