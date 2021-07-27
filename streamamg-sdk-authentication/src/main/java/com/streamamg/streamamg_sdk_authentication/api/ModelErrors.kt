package com.streamamg.streamamg_sdk_authentication.api

import com.google.gson.annotations.SerializedName

data class ModelErrors(
    @SerializedName("emailaddress")
    var emailaddress: String? = null,

    @SerializedName("password")
    var password: String? = null,

    @SerializedName("restriction")
    var restriction: String? = null,

    @SerializedName("Concurrency")
    var concurrency: String? = null
)