package com.streamamg.streamamg_sdk_purchases.models

import android.util.Log
import com.google.gson.annotations.SerializedName

data class PurchasePackagesResponse(
    @SerializedName("SubscriptionPlanOptions")
    val plans: ArrayList<IAPPlan>
) {
    fun packages(): ArrayList<String> {
        val data: ArrayList<String> = ArrayList()
        plans.forEach { plan ->
            plan.data?.find { x -> x.platform.equals("Google", true) }?.let { androidData ->
                Log.d("AMGCall", "Adding SKU ${androidData.productID} to the list of SKUs")
                data.add(androidData.productID)
            }
        }
        return data
    }
}

data class IAPPlan(
    @SerializedName("IAPData")
    val data: ArrayList<IAPData>?
)

data class IAPData(
    @SerializedName("Platform")
    val platform: String,
    @SerializedName("ProductID")
    val productID: String

)
