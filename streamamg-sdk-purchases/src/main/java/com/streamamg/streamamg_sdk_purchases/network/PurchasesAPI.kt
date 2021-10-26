package com.streamamg.streamamg_sdk_purchases.network

import com.streamamg.streamamg_sdk_authentication.api.LoginRequest
import com.streamamg.streamamg_sdk_authentication.api.LoginResponse
import com.streamamg.streamamg_sdk_purchases.models.PurchasePackagesResponse
import com.streamamg.streamamg_sdk_purchases.models.PurchasesValidationRequest
import com.streamamg.streamamg_sdk_purchases.models.PurchasesValidationResponse
import retrofit2.Call
import retrofit2.http.*

interface PurchasesAPI {

    @POST
    fun getValidation(@Url url: String, @Body validationRequest: PurchasesValidationRequest): Call<PurchasesValidationResponse>

    @GET
    fun getPackages(@Url url: String): Call<PurchasePackagesResponse>

}