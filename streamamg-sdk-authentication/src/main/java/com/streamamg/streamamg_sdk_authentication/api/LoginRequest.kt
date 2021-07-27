package com.streamamg.streamamg_sdk_authentication.api

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("emailaddress")
    val emailAddress: String,
    @SerializedName("password")
    val password: String
)