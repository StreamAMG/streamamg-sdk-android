package com.streamamg.streamamg_sdk_purchases.interfaces

import com.android.billingclient.api.Purchase

internal interface IAPInterface {
    fun updateIAPUI()
    fun validatePurchase(purchase: Purchase, isSub:Boolean)
    fun validatePurchase(purchase: Purchase, isSub:Boolean, jwToken: String?)
}