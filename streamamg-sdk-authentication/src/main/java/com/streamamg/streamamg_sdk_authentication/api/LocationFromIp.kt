package com.streamamg.streamamg_sdk_authentication.api

import com.google.gson.annotations.SerializedName

data class LocationFromIp(
    @SerializedName("Name")
    var name: String? = null,

    @SerializedName("Country")
    var country: String? = null,

    @SerializedName("CountryCode")
    var countryCode: String? = null,

    @SerializedName("State")
    var state: String? = null,

    @SerializedName("City")
    var city: String? = null
)