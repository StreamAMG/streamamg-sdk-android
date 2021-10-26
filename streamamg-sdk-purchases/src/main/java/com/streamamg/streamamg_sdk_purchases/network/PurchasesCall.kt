package com.streamamg.streamamg_sdk_purchases.network

import com.streamamg.streamamg_sdk_purchases.models.PurchasePackagesResponse
import com.streamamg.streamapi_core.models.StreamAMGError
import com.streamamg.streamamg_sdk_purchases.models.PurchasesValidationRequest
import com.streamamg.streamamg_sdk_purchases.models.PurchasesValidationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class PurchasesCall {
    internal fun validatePurchase(request: PurchasesValidationRequest, url: String) {
        val call = purchasesAPI.getValidation(url, request)
        call.enqueue(object : Callback<PurchasesValidationResponse> {
            override fun onFailure(call: Call<PurchasesValidationResponse>, t: Throwable) {
            }

            override fun onResponse(call: Call<PurchasesValidationResponse>, response: Response<PurchasesValidationResponse>) {
                if (response.isSuccessful) {
                    val model = response.body()
                    model?.let {
                        response(it)
                    }
                } else {
                    response(StreamAMGError(response.code(), response.message()))
                }
            }
        })
        return
    }

    internal fun callPackages(url: String) {
        val call = purchasesAPI.getPackages(url)
        call.enqueue(object : Callback<PurchasePackagesResponse> {
            override fun onFailure(call: Call<PurchasePackagesResponse>, t: Throwable) {
            }

            override fun onResponse(call: Call<PurchasePackagesResponse>, response: Response<PurchasePackagesResponse>) {
                if (response.isSuccessful) {
                    val model = response.body()
                    model?.let {
                        productResponse(it)
                    }
                } else {
                    productResponse(StreamAMGError(response.code(), response.message()))
                }
            }
        })
        return
    }

    internal open fun response(response: PurchasesValidationResponse) {}
    internal open fun response(response: StreamAMGError) {}
    internal open fun productResponse(response: PurchasePackagesResponse) {}
    internal open fun productResponse(response: StreamAMGError) {}

    companion object {
        lateinit var purchasesAPI: PurchasesAPI
    }
}